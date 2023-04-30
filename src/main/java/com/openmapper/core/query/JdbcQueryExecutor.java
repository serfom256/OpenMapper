package com.openmapper.core.query;

import com.openmapper.exceptions.internal.QueryExecutionError;

import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcQueryExecutor implements QueryExecutor {

    private final DataSource dataSource;

    public JdbcQueryExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public <T> T execute(final String query, final ResultSetHandler<T> handler, final Type returnType) throws SQLException {
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query); ResultSet rs = preparedStatement.executeQuery()) {
            return handler.handle(rs, returnType);
        } catch (SQLException e) {
            throw new QueryExecutionError(e);
        }
    }
}
