package com.openmapper.core.query.executors.operations;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import com.openmapper.common.operations.DmlOperation;
import com.openmapper.core.query.MethodSpecifications;

public interface DatabaseOperation {

    public Object executeQuery(
            PreparedStatement preparedStatement,
            MethodSpecifications methodSpecifications) throws SQLException;

    public Set<DmlOperation> getSupportedOperations();
}
