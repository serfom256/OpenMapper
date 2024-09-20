package com.openmapper.core.query.impl;

import com.openmapper.common.operations.DmlOperation;
import com.openmapper.core.query.PreparedStatementActions;
import com.openmapper.core.query.ResultSetHandler;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DmlOperationsHandler {

    private final PreparedStatementActions preparedStatementActions = new PreparedStatementActions();

    public <T> Object executeQuery(
            final ResultSetHandler<T> handler,
            final Type returnType,
            final DmlOperation operation,
            final PreparedStatement preparedStatement,
            final boolean shouldReturnGeneratedKeys) throws SQLException {

        if (operation == DmlOperation.SELECT) {
            return preparedStatementActions.executeQuery(handler, returnType, preparedStatement);
        } else {
            return preparedStatementActions.executeUpdate(
                    handler,
                    returnType,
                    preparedStatement,
                    shouldReturnGeneratedKeys);
        }
    }
}
