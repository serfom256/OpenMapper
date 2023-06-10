package com.openmapper.core.query.impl;

import com.openmapper.common.operations.DmlOperation;
import com.openmapper.core.query.ResultSetHandler;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DmlOperationsHandler {

    private DmlOperationsHandler() {
    }
    public static <T> Object evaluateOperation(final ResultSetHandler<T> handler, final Type returnType, DmlOperation operation, PreparedStatement preparedStatement) throws SQLException {
        if (operation == DmlOperation.SELECT) {
            ResultSet rs = preparedStatement.executeQuery();
            Object queryResult = handler.handle(rs, returnType);
            rs.close();
            return queryResult;
        } else {
            if (returnType == void.class || returnType == Void.class) return Void.TYPE;
            return preparedStatement.executeUpdate();
        }
    }

}
