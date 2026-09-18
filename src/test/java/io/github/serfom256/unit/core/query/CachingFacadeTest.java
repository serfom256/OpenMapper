package io.github.serfom256.unit.core.query;

import io.github.serfom256.annotations.Cached;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.cache.QueryCache;
import io.github.serfom256.common.reflect.ModelPropertyExtractor;
import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import io.github.serfom256.core.query.facade.OpenMapperCachingFacade;
import io.github.serfom256.unit.support.TestPlans;
import io.github.serfom256.core.query.model.BoundSql;
import io.github.serfom256.core.query.model.QueryParameters;
import io.github.serfom256.core.query.source.mapping.InputMapper;
import io.github.serfom256.parser.model.SQLRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CachingFacadeTest {

    @Mock ModelPropertyExtractor modelPropertyExtractor;
    @Mock OpenMapperGlobalEnvironmentVariables variables;
    @Mock InputMapper mapper;
    @Mock QueryCache queryCache;
    @Mock SQLRecord sqlRecord;

    interface TestDao {
        String find(@Param(name = "id") long id);

        @Cached
        String findCached(@Param(name = "id") long id);
    }

    @Test
    void getQuery_noCaching_alwaysBuildsQuery() throws Exception {
        when(mapper.mapSql(any(), any())).thenReturn(new BoundSql("SELECT 1", java.util.List.of()));
        OpenMapperCachingFacade facade = new OpenMapperCachingFacade(modelPropertyExtractor, variables, mapper, queryCache);

        Method m = TestDao.class.getDeclaredMethod("find", long.class);
        QueryParameters result = facade.getQuery(TestPlans.planFor(m), m, new Object[]{1L}, sqlRecord);

        assertNotNull(result);
        assertEquals("SELECT 1", result.getBoundSql().getSql());
        verify(queryCache, never()).get(any());
    }

    @Test
    void getQuery_cachingEnabled_storesResult() throws Exception {
        when(variables.isQueryCacheEnabled()).thenReturn(true);
        when(mapper.mapSql(any(), any())).thenReturn(new BoundSql("SELECT 1", java.util.List.of()));
        OpenMapperCachingFacade facade = new OpenMapperCachingFacade(modelPropertyExtractor, variables, mapper, queryCache);

        Method m = TestDao.class.getDeclaredMethod("find", long.class);
        facade.getQuery(TestPlans.planFor(m), m, new Object[]{1L}, sqlRecord);

        verify(queryCache).put(any(), any());
    }

    @Test
    void getQuery_cacheHit_returnsCachedResult() throws Exception {
        when(variables.isQueryCacheEnabled()).thenReturn(true);
        QueryParameters cached = new QueryParameters(new BoundSql("cached", java.util.List.of()), null, null);
        when(queryCache.get(any())).thenReturn(cached);
        OpenMapperCachingFacade facade = new OpenMapperCachingFacade(modelPropertyExtractor, variables, mapper, queryCache);

        Method m = TestDao.class.getDeclaredMethod("find", long.class);
        QueryParameters result = facade.getQuery(TestPlans.planFor(m), m, new Object[]{1L}, sqlRecord);

        // the cached query is reused without rebuilding; the returned wrapper is a
        // fresh one, because the caller mutates the specifications it carries
        assertSame(cached.getBoundSql(), result.getBoundSql());
        verify(mapper, never()).mapSql(any(), any());
    }

    @Test
    void getQuery_cachedAnnotation_usesCacheEvenWhenGloballyDisabled() throws Exception {
        when(variables.isQueryCacheEnabled()).thenReturn(false);
        when(mapper.mapSql(any(), any())).thenReturn(new BoundSql("SELECT 1", java.util.List.of()));
        OpenMapperCachingFacade facade = new OpenMapperCachingFacade(modelPropertyExtractor, variables, mapper, queryCache);

        Method m = TestDao.class.getDeclaredMethod("findCached", long.class);
        facade.getQuery(TestPlans.planFor(m), m, new Object[]{1L}, sqlRecord);

        verify(queryCache).put(any(), any());
    }

    @org.junit.jupiter.api.DisplayName("a cache hit must not hand back shared mutable state")
    @Test
    void cacheHit_returnsIndependentSpecifications() throws Exception {
        BoundSql boundSql = new BoundSql("SELECT 1", List.of());
        when(mapper.mapSql(any(), any())).thenReturn(boundSql);
        when(variables.isQueryCacheEnabled()).thenReturn(true);

        OpenMapperCachingFacade facade =
                new OpenMapperCachingFacade(modelPropertyExtractor, variables, mapper, queryCache);
        Method m = TestDao.class.getDeclaredMethod("find", long.class);

        QueryParameters first = facade.getQuery(TestPlans.planFor(m), m, new Object[]{1L}, sqlRecord);
        // the entry the facade stored is what a second caller would receive
        org.mockito.ArgumentCaptor<QueryParameters> stored =
                org.mockito.ArgumentCaptor.forClass(QueryParameters.class);
        verify(queryCache).put(any(), stored.capture());
        when(queryCache.get(any())).thenReturn(stored.getValue());

        QueryParameters second = facade.getQuery(TestPlans.planFor(m), m, new Object[]{1L}, sqlRecord);

        assertNotSame(first.getQuerySpecifications(), second.getQuerySpecifications(),
                "two callers share one QuerySpecifications, which each of them then mutates");

        // the immutable half may safely be shared
        assertSame(first.getBoundSql(), second.getBoundSql());

        // and mutating one must not be visible to the other
        second.getQuerySpecifications().setHasOptimisticLock(true);
        assertFalse(first.getQuerySpecifications().hasOptimisticLock());
    }
}
