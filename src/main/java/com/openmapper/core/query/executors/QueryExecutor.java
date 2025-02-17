package com.openmapper.core.query.executors;

import com.openmapper.core.query.model.QuerySpecifications;
import com.openmapper.exceptions.internal.QueryExecutionError;

public interface QueryExecutor {

    Object execute(
            final String query,
            final QuerySpecifications querySpecifications)
            throws QueryExecutionError;
}
