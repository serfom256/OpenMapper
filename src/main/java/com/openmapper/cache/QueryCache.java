package com.openmapper.cache;

import com.openmapper.core.query.model.QueryParameters;

public interface QueryCache {

    void put(int hash, QueryParameters queryEntry);

    QueryParameters get(int hash);
}