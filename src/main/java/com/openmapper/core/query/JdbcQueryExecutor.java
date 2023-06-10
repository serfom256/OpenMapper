package com.openmapper.core.query;

import com.openmapper.common.operations.DmlOperation;
import com.openmapper.core.query.impl.DmlOperationsHandler;
import com.openmapper.exceptions.internal.QueryExecutionError;

import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcQueryExecutor implements QueryExecutor {

    private final DataSource dataSource;

    public JdbcQueryExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public <T> Object execute(final String query, final ResultSetHandler<T> handler, final Type returnType, DmlOperation operation) {
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            return DmlOperationsHandler.evaluateOperation(handler, returnType, operation, preparedStatement);
        } catch (SQLException e) {
            throw new QueryExecutionError(e);
        }
    }
}
