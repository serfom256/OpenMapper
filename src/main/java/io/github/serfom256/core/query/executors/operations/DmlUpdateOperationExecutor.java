package io.github.serfom256.core.query.executors.operations;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import io.github.serfom256.core.query.common.PreparedStatementActions;
import io.github.serfom256.core.query.handlers.ResultSetHandler;
import io.github.serfom256.core.query.model.QuerySpecifications;
import io.github.serfom256.model.operations.DmlOperation;

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
    public Object executeBatch(
            PreparedStatement preparedStatement,
            QuerySpecifications methodSpecifications)
            throws SQLException {
        return preparedStatementActions.executeBatch(
                methodSpecifications.getHandler(),
                methodSpecifications.getReturnType(),
                preparedStatement,
                methodSpecifications.shouldReturnGeneratedKeys(),
                methodSpecifications.hasOptimisticLock());
    }

    @Override
    public Set<DmlOperation> getSupportedOperations() {
        return Set.of(DmlOperation.INSERT, DmlOperation.UPDATE, DmlOperation.DELETE, DmlOperation.MERGE);
    }
}
