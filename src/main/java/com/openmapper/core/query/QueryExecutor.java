package com.openmapper.core.query;

import com.openmapper.core.strategy.ResultSetHandler;

import java.lang.reflect.Type;
import java.sql.SQLException;

public interface QueryExecutor {

    <T> T execute(final String query, final ResultSetHandler<T> handler, final Type returnType) throws SQLException;
}
