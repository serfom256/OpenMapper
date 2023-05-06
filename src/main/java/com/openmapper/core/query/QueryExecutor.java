package com.openmapper.core.query;

import com.openmapper.exceptions.internal.QueryExecutionError;

import java.lang.reflect.Type;

public interface QueryExecutor {

    <T> T execute(final String query, final ResultSetHandler<T> handler, final Type returnType) throws QueryExecutionError;
}
