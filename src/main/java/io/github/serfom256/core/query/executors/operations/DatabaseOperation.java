package io.github.serfom256.core.query.executors.operations;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import io.github.serfom256.core.query.model.QuerySpecifications;
import io.github.serfom256.model.operations.DmlOperation;

public interface DatabaseOperation {

    public Object executeQuery(
            PreparedStatement preparedStatement,
            QuerySpecifications methodSpecifications) throws SQLException;

    default Object executeBatch(
            PreparedStatement preparedStatement,
            QuerySpecifications methodSpecifications) throws SQLException {
        throw new UnsupportedOperationException(
                "batch execution is not supported for " + getSupportedOperations());
    }

    public Set<DmlOperation> getSupportedOperations();
}
