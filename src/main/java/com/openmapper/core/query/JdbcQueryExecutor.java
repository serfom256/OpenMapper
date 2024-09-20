package com.openmapper.core.query;

import com.openmapper.common.operations.DmlOperation;
import com.openmapper.core.query.impl.DmlOperationsHandler;
import com.openmapper.exceptions.internal.QueryExecutionError;

import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcQueryExecutor implements QueryExecutor {

    private final DataSource dataSource;
    private final DmlOperationsHandler dmlOperationsHandler;

    public JdbcQueryExecutor(DataSource dataSource, DmlOperationsHandler dmlOperationsHandler) {
        this.dataSource = dataSource;
        this.dmlOperationsHandler = dmlOperationsHandler;
    }

    @Override
    public <T> Object execute(final String query, final ResultSetHandler<T> handler, final Type returnType, DmlOperation operation, int returnGeneratedKeys) {
        try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(query, new String[]{ "ID" })) {
            return dmlOperationsHandler.executeQuery(handler, returnType, operation, preparedStatement, returnGeneratedKeys == Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException e) {
            throw new QueryExecutionError(e);
        }
    }
}
