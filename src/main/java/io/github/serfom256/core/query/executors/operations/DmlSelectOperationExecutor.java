package io.github.serfom256.core.query.executors.operations;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import io.github.serfom256.core.query.common.PreparedStatementActions;
import io.github.serfom256.core.query.handlers.ResultSetHandler;
import io.github.serfom256.core.query.model.QuerySpecifications;
import io.github.serfom256.model.operations.DmlOperation;

public class DmlSelectOperationExecutor implements DatabaseOperation {

    private final PreparedStatementActions preparedStatementActions;

    public DmlSelectOperationExecutor(PreparedStatementActions preparedStatementActions) {
        this.preparedStatementActions = preparedStatementActions;
    }

    @Override
    public Object executeQuery(
            PreparedStatement preparedStatement,
            QuerySpecifications methodSpecifications)
            throws SQLException {
        ResultSetHandler<?> resultHandler = methodSpecifications.getHandler();
        Type returnType = methodSpecifications.getReturnType();
        return preparedStatementActions.executeQuery(resultHandler, returnType, preparedStatement);
    }

    @Override
    public Set<DmlOperation> getSupportedOperations() {
        return Set.of(DmlOperation.SELECT);
    }
}
