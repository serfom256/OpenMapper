package com.openmapper.core.query.facade;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.openmapper.annotations.DaoMethod;
import com.openmapper.common.operations.DmlOperation;
import com.openmapper.common.reflect.EntityPropertyExtractor;
import com.openmapper.config.OpenMapperGlobalEnvironmentVariables;
import com.openmapper.core.context.OpenMapperSQLContext;
import com.openmapper.core.query.MethodSpecifications;
import com.openmapper.core.query.executors.QueryExecutor;
import com.openmapper.core.query.handlers.ResultHandlerStrategy;
import com.openmapper.core.query.handlers.ResultSetHandler;
import com.openmapper.exceptions.internal.OptimisticLockException;
import com.openmapper.parser.mapping.InputMapper;
import com.openmapper.parser.model.SQLRecord;

@Component
public class QueryFacadeImpl implements QueryFacade {

    private final OpenMapperSQLContext context;
    private final InputMapper mapper;
    private final QueryExecutor queryExecutor;
    private final EntityPropertyExtractor entityPropertyExtractor;
    private final OpenMapperGlobalEnvironmentVariables variables;
    private final ResultHandlerStrategy resultHandlerStrategy;

    private static final Logger logger = LoggerFactory.getLogger(QueryFacadeImpl.class);

    public QueryFacadeImpl(
            OpenMapperSQLContext context,
            InputMapper mapper,
            QueryExecutor queryExecutor,
            EntityPropertyExtractor entityPropertyExtractor,
            OpenMapperGlobalEnvironmentVariables variables,
            ResultHandlerStrategy resultHandlerStrategy) {
        this.context = context;
        this.mapper = mapper;
        this.queryExecutor = queryExecutor;
        this.entityPropertyExtractor = entityPropertyExtractor;
        this.variables = variables;
        this.resultHandlerStrategy = resultHandlerStrategy;
    }

    @Override
    public Object invokeMethodQueryWithParameters(Method method, Object[] args, DataSource dataSource) {
        SQLRecord result = context.getSqlProcedure(getProcedureName(method));
        while (true) {
            final String query = mapper.mapSql(result, entityPropertyExtractor.extract(method, args));
            try {
                return executeDaoMethod(query, method, dataSource);
            } catch (OptimisticLockException optimisticLockException) {
                if (variables.isLogging()) {
                    logger.warn("Cannot execute update due to optimistic lock, trying again...");
                }
            }
        }
    }

    private Object executeDaoMethod(String query, Method method, DataSource dataSource) {
        Class<?> returnType = getReturnType(method);
        Type genericReturnType = getGenericReturnType(method);
        DmlOperation operationType = getOperationType(method);
        ResultSetHandler<?> resultSetHandler = resultHandlerStrategy.getExecutorByMethodReturnType(returnType);
        boolean returnGeneratedKeys = returnGeneratedKeys(method);

        MethodSpecifications methodSpecifications = new MethodSpecifications(
                resultSetHandler,
                genericReturnType,
                operationType,
                returnGeneratedKeys);

        Object result = queryExecutor.execute(dataSource, query, methodSpecifications);

        return method.getReturnType() == Optional.class ? Optional.ofNullable(result) : result;
    }

    private Class<?> getReturnType(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        if (method.getReturnType() == Optional.class) {
            return ((Class<?>) ((ParameterizedType) genericReturnType).getActualTypeArguments()[0]);
        }
        return method.getReturnType();
    }

    protected Type getGenericReturnType(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        if (method.getReturnType() == Optional.class) {
            return ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
        }
        return genericReturnType;
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

    private boolean returnGeneratedKeys(Method method) {
        DaoMethod daoMethod = method.getAnnotation(DaoMethod.class);
        return daoMethod != null && daoMethod.returnKeys();
    }
}
