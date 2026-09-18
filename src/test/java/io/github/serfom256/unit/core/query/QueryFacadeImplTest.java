package io.github.serfom256.unit.core.query;

import io.github.serfom256.common.reflect.ModelPropertyExtractor;
import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import io.github.serfom256.core.context.OpenMapperSQLContext;
import io.github.serfom256.core.query.executors.QueryExecutor;
import io.github.serfom256.core.query.facade.OpenMapperCachingFacade;
import io.github.serfom256.core.query.common.NotFoundTranslator;
import io.github.serfom256.core.query.method.DaoMethodPlanRegistry;
import io.github.serfom256.core.query.handlers.ResultSetHandlerStrategy;
import io.github.serfom256.core.query.facade.QueryFacadeImpl;
import io.github.serfom256.core.query.model.BoundSql;
import io.github.serfom256.core.query.model.QueryParameters;
import io.github.serfom256.core.query.model.QuerySpecifications;
import io.github.serfom256.exceptions.internal.OptimisticLockException;
import io.github.serfom256.model.operations.DmlOperation;
import io.github.serfom256.parser.model.SQLRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueryFacadeImplTest {

    @Mock OpenMapperSQLContext context;
    @Mock io.github.serfom256.core.query.source.mapping.InputMapper mapper;
    @Mock QueryExecutor queryExecutor;
    @Mock ModelPropertyExtractor modelPropertyExtractor;
    @Mock OpenMapperGlobalEnvironmentVariables variables;
    @Mock OpenMapperCachingFacade cachingFacade;
    @Mock DataSource dataSource;
    @Mock SQLRecord sqlRecord;

    private QueryFacadeImpl facade;

    interface TestDao {
        String find(long id);
        Optional<String> findOptional(long id);
    }

    private static DaoMethodPlanRegistry planRegistry() {
        return new DaoMethodPlanRegistry(io.github.serfom256.unit.support.TestPlans.strategy());
    }

    @BeforeEach
    void setUp() {
        facade = new QueryFacadeImpl(context, mapper, queryExecutor, modelPropertyExtractor, variables,
                cachingFacade, new NotFoundTranslator(), planRegistry(),
                io.github.serfom256.unit.support.TestPlans.strategy(),
                org.mockito.Mockito.mock(io.github.serfom256.core.query.facade.AssociationCascade.class),
                new io.github.serfom256.common.reflect.ModelFieldReader(),
                new io.github.serfom256.core.context.ModelMetadataContext());
    }

    @Test
    void invokeMethodQuery_successfulExecution() throws Exception {
        QuerySpecifications specs = new QuerySpecifications();
        specs.setOperation(DmlOperation.SELECT);
        QueryParameters params = new QueryParameters(new BoundSql("SELECT 1", List.of()), specs, sqlRecord);

        when(context.getSqlProcedure(any(), any())).thenReturn(sqlRecord);
        when(cachingFacade.getQuery(any(), any(), any(), any())).thenReturn(params);
        when(queryExecutor.execute(any(), any())).thenReturn("result");

        Method m = TestDao.class.getDeclaredMethod("find", long.class);
        Object result = facade.invokeMethodQueryWithParameters(m, new Object[]{1L}, dataSource);

        assertEquals("result", result);
        verify(queryExecutor).execute(argThat(b -> b.getSql().equals("SELECT 1")), eq(specs));
    }

    @Test
    void invokeMethodQuery_optionalReturn_wrapsInOptional() throws Exception {
        QuerySpecifications specs = new QuerySpecifications();
        specs.setOperation(DmlOperation.SELECT);
        QueryParameters params = new QueryParameters(new BoundSql("SELECT 1", List.of()), specs, sqlRecord);

        when(context.getSqlProcedure(any(), any())).thenReturn(sqlRecord);
        when(cachingFacade.getQuery(any(), any(), any(), any())).thenReturn(params);
        when(queryExecutor.execute(any(), any())).thenReturn("value");

        Method m = TestDao.class.getDeclaredMethod("findOptional", long.class);
        Object result = facade.invokeMethodQueryWithParameters(m, new Object[]{1L}, dataSource);

        assertInstanceOf(Optional.class, result);
        assertEquals("value", ((Optional<?>) result).orElse(null));
    }

    @Test
    void invokeMethodQuery_optionalReturn_nullWrapsAsEmpty() throws Exception {
        QuerySpecifications specs = new QuerySpecifications();
        specs.setOperation(DmlOperation.SELECT);
        QueryParameters params = new QueryParameters(new BoundSql("SELECT 1", List.of()), specs, sqlRecord);

        when(context.getSqlProcedure(any(), any())).thenReturn(sqlRecord);
        when(cachingFacade.getQuery(any(), any(), any(), any())).thenReturn(params);
        when(queryExecutor.execute(any(), any())).thenReturn(null);

        Method m = TestDao.class.getDeclaredMethod("findOptional", long.class);
        Object result = facade.invokeMethodQueryWithParameters(m, new Object[]{1L}, dataSource);

        assertInstanceOf(Optional.class, result);
        assertTrue(((Optional<?>) result).isEmpty());
    }

    @Test
    void invokeMethodQuery_optimisticLockConflict_propagatesWithoutRetrying() throws Exception {
        QuerySpecifications specs = new QuerySpecifications();
        specs.setOperation(DmlOperation.UPDATE);
        QueryParameters params = new QueryParameters(new BoundSql("UPDATE x", List.of()), specs, sqlRecord);

        when(context.getSqlProcedure(any(), any())).thenReturn(sqlRecord);
        when(cachingFacade.getQuery(any(), any(), any(), any())).thenReturn(params);
        when(queryExecutor.execute(any(), any())).thenThrow(new OptimisticLockException());

        Method m = TestDao.class.getDeclaredMethod("find", long.class);
        assertThrows(OptimisticLockException.class,
                () -> facade.invokeMethodQueryWithParameters(m, new Object[]{1L}, dataSource));

        verify(queryExecutor, times(1)).execute(any(), any());
    }

    @Test
    void invokeMethodQuery_setsDataSourceOnSpecs() throws Exception {
        QuerySpecifications specs = new QuerySpecifications();
        specs.setOperation(DmlOperation.SELECT);
        QueryParameters params = new QueryParameters(new BoundSql("SELECT 1", List.of()), specs, sqlRecord);

        when(context.getSqlProcedure(any(), any())).thenReturn(sqlRecord);
        when(cachingFacade.getQuery(any(), any(), any(), any())).thenReturn(params);
        when(queryExecutor.execute(any(), any())).thenReturn("ok");

        Method m = TestDao.class.getDeclaredMethod("find", long.class);
        facade.invokeMethodQueryWithParameters(m, new Object[]{1L}, dataSource);

        assertSame(dataSource, specs.getDataSource());
    }
}
