package com.openmapper.core.query.executors;

import com.openmapper.core.query.MethodSpecifications;
import com.openmapper.core.query.executors.operations.DatabaseOperation;
import com.openmapper.exceptions.internal.QueryExecutionError;

import javax.sql.DataSource;

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
    public Object execute(
            DataSource dataSource,
            String query,
            MethodSpecifications methodSpecifications) {

        int returnGeneratedKeys = methodSpecifications.shouldReturnGeneratedKeys() ? 1 : 0;
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query, returnGeneratedKeys)) {
            
            DatabaseOperation databaseOperation = queryExecutionStrategy.getOperationByType(methodSpecifications.getOperation());
            return databaseOperation.executeQuery(preparedStatement, methodSpecifications);
        } catch (SQLException e) {
            throw new QueryExecutionError(e);
        }
    }
}
