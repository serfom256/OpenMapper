package com.openmapper.core.query.facade;

import java.lang.reflect.Method;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.openmapper.common.reflect.ModelPropertyExtractor;
import com.openmapper.config.OpenMapperGlobalEnvironmentVariables;
import com.openmapper.core.context.OpenMapperSQLContext;
import com.openmapper.core.query.executors.QueryExecutor;
import com.openmapper.core.query.model.QuerySpecifications;
import com.openmapper.exceptions.internal.OptimisticLockException;
import com.openmapper.parser.mapping.InputMapper;
import com.openmapper.parser.model.SQLRecord;

@Component
public class QueryFacadeImpl implements QueryFacade {

    private final OpenMapperSQLContext context;
    private final InputMapper mapper;
    private final QueryExecutor queryExecutor;
    private final ModelPropertyExtractor modelPropertyExtractor;
    private final OpenMapperGlobalEnvironmentVariables variables;

    private static final Logger logger = LoggerFactory.getLogger(QueryFacadeImpl.class);
    private static final int OPTIMISTIC_LOCK_RETRIES_DEFAULT_COUNT = 100;

    public QueryFacadeImpl(
            OpenMapperSQLContext context,
            InputMapper mapper,
            QueryExecutor queryExecutor,
            ModelPropertyExtractor modelPropertyExtractor,
            OpenMapperGlobalEnvironmentVariables variables) {
        this.context = context;
        this.mapper = mapper;
        this.queryExecutor = queryExecutor;
        this.modelPropertyExtractor = modelPropertyExtractor;
        this.variables = variables;
    }

    @Override
    public Object invokeMethodQueryWithParameters(Method method, Object[] args, DataSource dataSource) {
        SQLRecord result = context.getSqlProcedure(method);
        QuerySpecifications querySpecifications = new QuerySpecifications();
        int optimisticLockRetries = OPTIMISTIC_LOCK_RETRIES_DEFAULT_COUNT;

        while (optimisticLockRetries-- > 0) {
            modelPropertyExtractor.extractQuerySpecifications(method, args, querySpecifications);
            final String query = mapper.mapSql(result, querySpecifications.getParams());

            try {
                return executeDaoMethod(query, method, dataSource, querySpecifications);
            } catch (OptimisticLockException optimisticLockException) {
                if (variables.isLogging()) {
                    logger.warn("Cannot execute update due to optimistic lock, trying again...");
                }
            }
        }

        throw new OptimisticLockException("Optimistic lock retires count exceeded");
    }

    private Object executeDaoMethod(String query, Method method, DataSource dataSource,
            QuerySpecifications querySpecifications) {
        Object result = queryExecutor.execute(dataSource, query, querySpecifications);

        return method.getReturnType() == Optional.class ? Optional.ofNullable(result) : result;
    }
}
