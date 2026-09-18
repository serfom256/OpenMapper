package io.github.serfom256.core.query.facade;

import java.lang.reflect.Method;

import io.github.serfom256.cache.QueryCache;
import io.github.serfom256.cache.QueryCacheKey;
import io.github.serfom256.common.reflect.ModelPropertyExtractor;
import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import io.github.serfom256.core.query.method.DaoMethodPlan;
import io.github.serfom256.core.query.model.BoundSql;
import io.github.serfom256.core.query.model.QueryParameters;
import io.github.serfom256.core.query.model.QuerySpecifications;
import io.github.serfom256.core.query.source.mapping.InputMapper;
import io.github.serfom256.parser.model.SQLRecord;

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

    public QueryParameters getQuery(DaoMethodPlan plan, Method method, Object[] args, SQLRecord sqlRecord) {

        final boolean useCache = plan.isCached() || variables.isQueryCacheEnabled();
        QueryCacheKey key = null;

        if (useCache) {
            key = new QueryCacheKey(method, args);
            QueryParameters queryParameters = queryCache.get(key);
            if (queryParameters != null) {
                QuerySpecifications cached = queryParameters.getQuerySpecifications();
                return new QueryParameters(queryParameters.getBoundSql(),
                        cached == null ? null : cached.copy(),
                        queryParameters.getSource());
            }
        }

        final QuerySpecifications querySpecifications = new QuerySpecifications();
        modelPropertyExtractor.extractQuerySpecifications(plan, args, querySpecifications);
        final BoundSql boundSql = mapper.mapSql(sqlRecord, querySpecifications.getParams());
        final QueryParameters queryParameters = new QueryParameters(boundSql, querySpecifications, sqlRecord);

        if (useCache) {
            queryCache.put(key, queryParameters);
        }
        return queryParameters;
    }
}
