package io.github.serfom256.cache;

import io.github.serfom256.core.query.model.QueryParameters;

public interface QueryCache {

    void put(QueryCacheKey key, QueryParameters queryEntry);

    QueryParameters get(QueryCacheKey key);
}
