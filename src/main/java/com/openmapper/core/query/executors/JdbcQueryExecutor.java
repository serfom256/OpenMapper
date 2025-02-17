package com.openmapper.core.query.executors;

import com.openmapper.core.query.executors.operations.DatabaseOperation;
import com.openmapper.core.query.model.QuerySpecifications;
import com.openmapper.exceptions.internal.QueryExecutionError;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component
public class JdbcQueryExecutor implements QueryExecutor {

    private final QueryExecutionStrategy queryExecutionStrategy;

    public JdbcQueryExecutor(QueryExecutionStrategy queryExecutionStrategy) {
        this.queryExecutionStrategy = queryExecutionStrategy;
    }

    @Override
    public Object execute(String query, QuerySpecifications querySpecifications) {
        int returnGeneratedKeys = querySpecifications.shouldReturnGeneratedKeys() ? 1 : 0;

        try (Connection connection = querySpecifications.getDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query, returnGeneratedKeys)) {

            DatabaseOperation dbOps = queryExecutionStrategy.getOperationByType(querySpecifications.getOperation());
            return dbOps.executeQuery(preparedStatement, querySpecifications);
        } catch (SQLException e) {
            throw new QueryExecutionError(e);
        }
    }
}
