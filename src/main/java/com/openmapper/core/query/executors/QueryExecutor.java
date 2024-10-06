package com.openmapper.core.query.executors;

import javax.sql.DataSource;

import com.openmapper.core.query.model.QuerySpecifications;
import com.openmapper.exceptions.internal.QueryExecutionError;

public interface QueryExecutor {

    Object execute(
            final DataSource dataSource,
            final String query,
            final QuerySpecifications querySpecifications)
            throws QueryExecutionError;
}
