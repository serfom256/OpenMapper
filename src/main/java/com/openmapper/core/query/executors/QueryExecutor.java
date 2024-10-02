package com.openmapper.core.query.executors;

import javax.sql.DataSource;

import com.openmapper.core.query.MethodSpecifications;
import com.openmapper.exceptions.internal.QueryExecutionError;

public interface QueryExecutor {

    Object execute(
            final DataSource dataSource,
            final String query,
            final MethodSpecifications methodSpecifications)
            throws QueryExecutionError;
}
