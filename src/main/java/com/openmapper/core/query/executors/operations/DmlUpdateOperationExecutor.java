package com.openmapper.core.query.executors.operations;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.openmapper.common.operations.DmlOperation;
import com.openmapper.core.query.common.PreparedStatementActions;
import com.openmapper.core.query.handlers.ResultSetHandler;
import com.openmapper.core.query.model.QuerySpecifications;

@Component
public class DmlUpdateOperationExecutor implements DatabaseOperation {

    private final PreparedStatementActions preparedStatementActions;

    public DmlUpdateOperationExecutor(PreparedStatementActions preparedStatementActions) {
        this.preparedStatementActions = preparedStatementActions;
    }

    @Override
    public Object executeQuery(
            PreparedStatement preparedStatement,
            QuerySpecifications methodSpecifications)
            throws SQLException {
        Type returnType = methodSpecifications.getReturnType();
        boolean shouldReturnGeneratedKeys = methodSpecifications.shouldReturnGeneratedKeys();
        boolean hasPreviousOptimisticLockValue = methodSpecifications.hasOptimisticLock();
        ResultSetHandler<?> resultHandler = methodSpecifications.getHandler();

        return preparedStatementActions.executeUpdate(
                resultHandler,
                returnType,
                preparedStatement,
                shouldReturnGeneratedKeys,
                hasPreviousOptimisticLockValue);
    }

    @Override
    public Set<DmlOperation> getSupportedOperations() {
        return Set.of(DmlOperation.INSERT, DmlOperation.UPDATE, DmlOperation.DELETE, DmlOperation.MERGE);
    }
}
