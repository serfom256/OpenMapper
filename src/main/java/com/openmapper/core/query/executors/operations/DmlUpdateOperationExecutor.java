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
import com.openmapper.exceptions.internal.OptimisticLockException;

@Component
public class DmlUpdateOperationExecutor implements DatabaseOperation {

    private final PreparedStatementActions preparedStatementActions;

    public DmlUpdateOperationExecutor(PreparedStatementActions preparedStatementActions) {
        this.preparedStatementActions = preparedStatementActions;
    }

    @Override
    public Object executeQuery(
            PreparedStatement preparedStatement,
            MethodSpecifications methodSpecifications)
            throws SQLException {
        ResultSetHandler<?> resultHandler = methodSpecifications.getHandler();
        Type returnType = methodSpecifications.getReturnType();
        Object queryResult = preparedStatementActions.executeQuery(resultHandler, returnType, preparedStatement);

        if(methodSpecifications.shouldReturnGeneratedKeys() && queryResult == null){
            throw new OptimisticLockException();
        }
        return queryResult;
    }

    @Override
    public Set<DmlOperation> getSupportedOperations() {
        return Set.of(DmlOperation.INSERT, DmlOperation.UPDATE, DmlOperation.DELETE, DmlOperation.MERGE);
    }
}
