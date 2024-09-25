package com.openmapper.core.facade;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import com.openmapper.annotations.DaoMethod;
import com.openmapper.common.entity.SQLRecord;
import com.openmapper.common.mapping.InputMapper;
import com.openmapper.common.operations.DmlOperation;
import com.openmapper.common.reflect.EntityPropertyExtractor;
import com.openmapper.core.OpenMapperSQLContext;
import com.openmapper.core.query.JdbcQueryExecutor;
import com.openmapper.core.query.QueryExecutor;
import com.openmapper.core.query.QueryExecutorStrategy;
import com.openmapper.core.query.impl.DmlOperationsHandler;

@Component
public class QueryFacadeImpl implements QueryFacade {

    private final OpenMapperSQLContext context;
    private final InputMapper mapper;
    private final QueryExecutorStrategy strategy;
    private final QueryExecutor queryExecutor;
    private final EntityPropertyExtractor entityPropertyExtractor;

    public QueryFacadeImpl(
            OpenMapperSQLContext context,
            QueryExecutorStrategy strategy,
            InputMapper mapper,
            DataSource dataSource,
            EntityPropertyExtractor methodArgumentsExtractor) {
        this.context = context;
        this.strategy = strategy;
        this.mapper = mapper;
        this.entityPropertyExtractor = methodArgumentsExtractor;
        this.queryExecutor = new JdbcQueryExecutor(dataSource, new DmlOperationsHandler());
    }

    @Override
    public Object invokeMethodQueryWithParameters(Method method, Object[] args) {
        int argumentsHash = Arrays.hashCode(args);

        SQLRecord result = context.getSqlProcedure(getProcedureName(method));
        final String query = mapper.mapSql(result, entityPropertyExtractor.extract(method, args));
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

        Object result = queryExecutor.execute(
                query,
                strategy.getExecutorByMethodReturnType(returnType),
                genericReturnType,
                getOperationType(method),
                returnGeneratedKeys(method));

        return method.getReturnType() == Optional.class ? Optional.ofNullable(result) : result;
    }

    private int returnGeneratedKeys(Method method) {
        DaoMethod daoMethod = method.getAnnotation(DaoMethod.class);
        if (daoMethod == null) {
            return Statement.NO_GENERATED_KEYS;
        }
        return !daoMethod.returnKeys() ? Statement.NO_GENERATED_KEYS : Statement.RETURN_GENERATED_KEYS;
    }

    private DmlOperation getOperationType(Method method) {
        DmlOperation operation = DmlOperation.SELECT;
        DaoMethod daoMethod = method.getAnnotation(DaoMethod.class);
        if (daoMethod != null) {
            operation = daoMethod.operation();
        }
        return operation;
    }

    private String getProcedureName(Method method) { // TODO move to another class
        DaoMethod daoMethod = method.getAnnotation(DaoMethod.class);
        String procedure = method.getName();
        if (daoMethod != null && !daoMethod.procedure().isEmpty()) {
            procedure = daoMethod.procedure();
        }
        return procedure;
    }

}
