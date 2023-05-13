package com.openmapper.core.proxy;

import com.openmapper.core.OpenMapperSqlContext;
import com.openmapper.core.annotations.DaoMethod;
import com.openmapper.core.annotations.Param;
import com.openmapper.core.entity.FsqlEntity;
import com.openmapper.core.query.JdbcQueryExecutor;
import com.openmapper.core.query.QueryExecutor;
import com.openmapper.core.query.QueryExecutorStrategy;
import com.openmapper.core.resources.mapping.InputMapper;

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


    public EntityMappingInvocationHandler(OpenMapperSqlContext context, QueryExecutorStrategy strategy, InputMapper mapper, DataSource dataSource) {
        this.context = context;
        this.strategy = strategy;
        this.queryExecutor = new JdbcQueryExecutor(dataSource);
        this.mapper = mapper;
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
        Class<?> returnType; // TODO add generic entity support
        Type genericReturnType = method.getGenericReturnType();
        if (method.getReturnType() == Optional.class) { // FIXME handler ClassCastException if optional contains generic iterable class
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
            Param annotation = parameter.getAnnotation(Param.class);
            if (annotation == null) {
                params.put(parameter.getName(), args[i]);
            } else {
                params.put(annotation.name(), args[i]);
            }
        }
        return params;
    }
}
