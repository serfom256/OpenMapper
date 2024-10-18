package com.openmapper.core.query.facade;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.stereotype.Component;

import com.openmapper.annotations.Cached;
import com.openmapper.cache.QueryCache;
import com.openmapper.common.reflect.ModelPropertyExtractor;
import com.openmapper.config.OpenMapperGlobalEnvironmentVariables;
import com.openmapper.core.query.model.QueryParameters;
import com.openmapper.core.query.model.QuerySpecifications;
import com.openmapper.core.query.source.mapping.InputMapper;
import com.openmapper.parser.model.SQLRecord;

@Component
public class OpenMapperCachingFacade {

    private final ModelPropertyExtractor modelPropertyExtractor;
    private final OpenMapperGlobalEnvironmentVariables variables;
    private final InputMapper mapper;
    private final QueryCache queryCache;

    public OpenMapperCachingFacade(
            ModelPropertyExtractor modelPropertyExtractor,
            OpenMapperGlobalEnvironmentVariables variables,
            InputMapper mapper,
            QueryCache queryCache) {
        this.modelPropertyExtractor = modelPropertyExtractor;
        this.variables = variables;
        this.mapper = mapper;
        this.queryCache = queryCache;
    }

    public QueryParameters getQuery(Method method, Object[] args, SQLRecord sqlRecord) {

        final boolean useCache = method.getAnnotation(Cached.class) != null || variables.isQueryCacheEnabled();
        int hash = 0;

        if (useCache) {
            hash = getHash(method, args);
            QueryParameters queryParameters = queryCache.get(hash);
            if (queryParameters != null) {
                return queryParameters;
            }
        }

        final QuerySpecifications querySpecifications = new QuerySpecifications();
        modelPropertyExtractor.extractQuerySpecifications(method, args, querySpecifications);
        final String query = mapper.mapSql(sqlRecord, querySpecifications.getParams());
        final QueryParameters queryParameters = new QueryParameters(query, querySpecifications, sqlRecord);

        if (useCache) {
            queryCache.put(hash, queryParameters);
        }
        return queryParameters;
    }

    private int getHash(Method method, Object[] args) {
        return Arrays.hashCode(args) * 31 + method.hashCode();
    }
}
