package io.github.serfom256.unit.core.common;

import io.github.serfom256.annotations.entity.Sql;
import io.github.serfom256.core.common.DaoMethodFacade;
import io.github.serfom256.core.context.DaoMethodContext;
import io.github.serfom256.core.query.method.DaoMethodPlan;
import io.github.serfom256.unit.support.TestPlans;
import io.github.serfom256.parser.SourceMapper;
import io.github.serfom256.parser.model.SQLRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DaoMethodFacadeTest {

    @Mock DaoMethodContext daoMethodContext;
    @Mock SourceMapper sourceMapper;

    interface TestDao {
        void executeQuery(@Sql String sql, int param);
        void noSqlParam(int x);
    }

    @Test
    void plan_locatesTheSqlParameter() {
        assertTrue(TestPlans.planFor(TestDao.class, "executeQuery", String.class, int.class)
                .hasEmbeddedSql());
        assertFalse(TestPlans.planFor(TestDao.class, "noSqlParam", int.class).hasEmbeddedSql());
    }

    @Test
    void extractProcedure_cachesResult() throws Exception {
        DaoMethodFacade facade = new DaoMethodFacade(daoMethodContext, sourceMapper);
        DaoMethodPlan plan = TestPlans.planFor(TestDao.class, "executeQuery", String.class, int.class);

        SQLRecord record = new SQLRecord(List.of(), Map.of());
        when(daoMethodContext.getSqlRecordByProcedure("SELECT 1")).thenReturn(null);
        when(sourceMapper.map("SELECT 1")).thenReturn(record);

        SQLRecord result = facade.extractProcedureFromMethodArguments(plan, new Object[]{"SELECT 1", 42});
        assertSame(record, result);
        verify(daoMethodContext).registerSqlProcedure("SELECT 1", record);
    }

    @Test
    void extractProcedure_returnsCachedOnSecondCall() throws Exception {
        DaoMethodFacade facade = new DaoMethodFacade(daoMethodContext, sourceMapper);
        DaoMethodPlan plan = TestPlans.planFor(TestDao.class, "executeQuery", String.class, int.class);

        SQLRecord cached = new SQLRecord(List.of(), Map.of());
        when(daoMethodContext.getSqlRecordByProcedure("SELECT 1")).thenReturn(cached);

        SQLRecord result = facade.extractProcedureFromMethodArguments(plan, new Object[]{"SELECT 1", 42});
        assertSame(cached, result);
        verify(sourceMapper, never()).map(any());
    }

    @Test
    void extractProcedure_noSqlAnnotation_throws() throws Exception {
        DaoMethodFacade facade = new DaoMethodFacade(daoMethodContext, sourceMapper);
        DaoMethodPlan plan = TestPlans.planFor(TestDao.class, "noSqlParam", int.class);

        assertThrows(IllegalStateException.class,
                () -> facade.extractProcedureFromMethodArguments(plan, new Object[]{1}));
    }
}
