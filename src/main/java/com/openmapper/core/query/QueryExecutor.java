package com.openmapper.core.query;

import com.openmapper.common.DmlOperation;
import com.openmapper.exceptions.internal.QueryExecutionError;

import java.lang.reflect.Type;

public interface QueryExecutor {

    <T> Object execute(final String query, final ResultSetHandler<T> handler, final Type returnType, DmlOperation operation) throws QueryExecutionError;
}
