package com.openmapper.core.proxy;

import com.openmapper.core.OpenMapperSqlContext;
import com.openmapper.core.annotations.DaoMethod;
import com.openmapper.core.annotations.Param;
import com.openmapper.core.entity.FsqlEntity;
import com.openmapper.core.files.mapping.InputMapper;
import com.openmapper.core.query.JdbcQueryExecutor;
import com.openmapper.core.query.QueryExecutor;
import com.openmapper.core.query.QueryExecutorStrategy;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;


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
        if (annotatedMethodName == null) {
            throw new IllegalStateException(String.format("Method %s doesn't annotated with @DaoMethod", method.getName()));
        }
        FsqlEntity result = context.getSql(annotatedMethodName.procedure());
        final String query = mapper.mapSql(result, extractMethodParams(method, args));
        return queryExecutor.execute(query, strategy.getExecutorByMethodReturnType(method.getReturnType()), method.getGenericReturnType());
    }

    private Map<String, Object> extractMethodParams(Method method, Object[] args) {
        Parameter[] methodParams = method.getParameters();
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < methodParams.length; i++) {
            Parameter parameter = methodParams[i];
            Annotation[] annotArr = parameter.getAnnotations();
            for (Annotation annot : annotArr) {
                if (annot instanceof Param) {
                    params.put(((Param) annot).name(), args[i]);
                }
            }
        }
        return params;
    }
}
