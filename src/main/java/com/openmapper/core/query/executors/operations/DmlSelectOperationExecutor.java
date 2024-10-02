package com.openmapper.core.query.executors.operations;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.openmapper.common.operations.DmlOperation;
import com.openmapper.core.query.MethodSpecifications;
import com.openmapper.core.query.common.PreparedStatementActions;
import com.openmapper.core.query.handlers.ResultSetHandler;

@Component
public class DmlSelectOperationExecutor implements DatabaseOperation {

    private final PreparedStatementActions preparedStatementActions;

    public DmlSelectOperationExecutor(PreparedStatementActions preparedStatementActions) {
        this.preparedStatementActions = preparedStatementActions;
    }

    @Override
    public Object executeQuery(
            PreparedStatement preparedStatement,
            MethodSpecifications methodSpecifications)
            throws SQLException {
        Type returnType = methodSpecifications.getReturnType();
        boolean shouldReturnGeneratedKeys = methodSpecifications.shouldReturnGeneratedKeys();
        ResultSetHandler<?> resultHandler = methodSpecifications.getHandler();

        return preparedStatementActions.executeUpdate(
                resultHandler,
                returnType,
                preparedStatement,
                shouldReturnGeneratedKeys);
    }

    @Override
    public Set<DmlOperation> getSupportedOperations() {
        return Set.of(DmlOperation.SELECT);
    }
}
