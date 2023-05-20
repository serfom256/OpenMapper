package com.openmapper.core.proxy;

import com.openmapper.core.OpenMapperSqlContext;
import com.openmapper.core.annotations.DaoMethod;
import com.openmapper.core.annotations.Param;
import com.openmapper.core.annotations.entity.Entity;
import com.openmapper.core.entity.FsqlEntity;
import com.openmapper.core.query.JdbcQueryExecutor;
import com.openmapper.core.query.QueryExecutor;
import com.openmapper.core.query.QueryExecutorStrategy;
import com.openmapper.core.processors.mapping.InputMapper;
import com.openmapper.mappers.EntityPropertyExtractor;

import javax.sql.DataSource;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class EntityMappingInvocationHandler implements InvocationHandler {

    private final OpenMapperSqlContext context;
    private final InputMapper mapper;
    private final QueryExecutorStrategy strategy;
    private final QueryExecutor queryExecutor;


    public EntityMappingInvocationHandler(OpenMapperSqlContext context,
                                          QueryExecutorStrategy strategy,
                                          InputMapper mapper,
                                          DataSource dataSource) {
        this.context = context;
        this.strategy = strategy;
        this.mapper = mapper;
        this.queryExecutor = new JdbcQueryExecutor(dataSource);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        DaoMethod annotatedMethodName = method.getAnnotation(DaoMethod.class);
        String procedure = method.getName();
        if (annotatedMethodName != null) {
            procedure = annotatedMethodName.procedure();
        }
        FsqlEntity result = context.getSql(procedure);
        final String query = mapper.mapSql(result, extractMethodParams(method, args));
        return handlerOptionalReturnType(query, method);
    }

    private Object handlerOptionalReturnType(String query, Method method) {
        Class<?> returnType;
        Type genericReturnType = method.getGenericReturnType();
        if (method.getReturnType() == Optional.class) {
            returnType = ((Class<?>) ((ParameterizedType) genericReturnType).getActualTypeArguments()[0]);
            genericReturnType = ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
        } else {
            returnType = method.getReturnType();
        }
        Object result = queryExecutor.execute(query, strategy.getExecutorByMethodReturnType(returnType), genericReturnType);
        return method.getReturnType() == Optional.class ? Optional.ofNullable(result) : result;
    }

    private Map<String, Object> extractMethodParams(Method method, Object[] args) {
        Parameter[] methodParams = method.getParameters();
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < methodParams.length; i++) {
            Parameter parameter = methodParams[i];
            if (parameter.getType().getAnnotation(Entity.class) != null) {
                params.putAll(EntityPropertyExtractor.extractParams(args[i]));
            } else {
                Param annotation = parameter.getAnnotation(Param.class);
                params.put(annotation == null ? parameter.getName() : annotation.name(), args[i]);
            }
        }
        return params;
    }
}
