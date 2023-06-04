package com.openmapper.core;

import com.openmapper.common.DmlOperation;
import com.openmapper.common.MethodArgumentsExtractor;
import com.openmapper.annotations.DaoMethod;
import com.openmapper.core.entity.SQLRecord;
import com.openmapper.common.mapping.InputMapper;
import com.openmapper.core.query.JdbcQueryExecutor;
import com.openmapper.core.query.QueryExecutor;
import com.openmapper.core.query.QueryExecutorStrategy;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
        SQLRecord result = context.getSql(getProcedureName(method));
        final String query = mapper.mapSql(result, MethodArgumentsExtractor.extractNamedArgs(method, args));
        return executeDaoMethod(query, method);
    }

    private Object executeDaoMethod(String query, Method method) {
        Class<?> returnType;
        Type genericReturnType = method.getGenericReturnType();
        if (method.getReturnType() == Optional.class) {
            returnType = ((Class<?>) ((ParameterizedType) genericReturnType).getActualTypeArguments()[0]);
            genericReturnType = ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
        } else {
            returnType = method.getReturnType();
        }

        Object result = queryExecutor.execute(query, strategy.getExecutorByMethodReturnType(returnType), genericReturnType, getOperationType(method));
        return method.getReturnType() == Optional.class ? Optional.ofNullable(result) : result;
    }

    private DmlOperation getOperationType(Method method) {
        DmlOperation operation = DmlOperation.SELECT;
        DaoMethod daoMethod = method.getAnnotation(DaoMethod.class);
        if (daoMethod != null) {
            operation = daoMethod.operation();
        }
        return operation;
    }

    private String getProcedureName(Method method) {
        DaoMethod daoMethod = method.getAnnotation(DaoMethod.class);
        String procedure = method.getName();
        if (daoMethod != null && !daoMethod.procedure().isEmpty()) {
            procedure = daoMethod.procedure();
        }
        return procedure;
    }

}
