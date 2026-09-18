package io.github.serfom256.core.query.executors;

import org.springframework.dao.DataAccessException;

import io.github.serfom256.core.query.model.BoundSql;
import io.github.serfom256.core.query.model.QuerySpecifications;

public interface QueryExecutor {

    Object execute(
            final BoundSql boundSql,
            final QuerySpecifications querySpecifications)
            throws DataAccessException;

    Object executeBatch(
            final java.util.List<BoundSql> statements,
            final QuerySpecifications querySpecifications)
            throws DataAccessException;
}
