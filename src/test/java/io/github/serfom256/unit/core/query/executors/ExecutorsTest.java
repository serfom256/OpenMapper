package io.github.serfom256.unit.core.query.executors;

import io.github.serfom256.core.query.common.PreparedStatementActions;
import io.github.serfom256.core.query.executors.QueryExecutionStrategy;
import io.github.serfom256.core.query.executors.operations.DatabaseOperation;
import io.github.serfom256.core.query.executors.operations.DmlSelectOperationExecutor;
import io.github.serfom256.core.query.executors.operations.DmlUpdateOperationExecutor;
import io.github.serfom256.core.query.handlers.ResultSetHandler;
import io.github.serfom256.core.query.model.QuerySpecifications;
import io.github.serfom256.exceptions.internal.OptimisticLockException;
import io.github.serfom256.model.operations.DmlOperation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecutorsTest {

    @Nested
    class QueryExecutionStrategyTest {

        @Test
        void getOperation_registered_returnsCorrectOp() {
            DatabaseOperation selectOp = mock(DatabaseOperation.class);
            when(selectOp.getSupportedOperations()).thenReturn(Set.of(DmlOperation.SELECT));
            DatabaseOperation updateOp = mock(DatabaseOperation.class);
            when(updateOp.getSupportedOperations()).thenReturn(Set.of(DmlOperation.INSERT, DmlOperation.UPDATE, DmlOperation.DELETE));

            QueryExecutionStrategy strategy = new QueryExecutionStrategy(List.of(selectOp, updateOp));

            assertSame(selectOp, strategy.getOperationByType(DmlOperation.SELECT));
            assertSame(updateOp, strategy.getOperationByType(DmlOperation.INSERT));
            assertSame(updateOp, strategy.getOperationByType(DmlOperation.UPDATE));
            assertSame(updateOp, strategy.getOperationByType(DmlOperation.DELETE));
        }

        @Test
        void getOperation_unregistered_throws() {
            QueryExecutionStrategy strategy = new QueryExecutionStrategy(List.of());
            assertThrows(IllegalStateException.class,
                    () -> strategy.getOperationByType(DmlOperation.SELECT));
        }
    }

    @Nested
    class PreparedStatementActionsTest {

        @Mock PreparedStatement ps;
        @Mock ResultSet resultSet;
        @Mock ResultSetHandler<Object> handler;

        private final PreparedStatementActions actions = new PreparedStatementActions();

        @Test
        void executeQuery_delegatesToHandler() throws SQLException {
            when(ps.executeQuery()).thenReturn(resultSet);
            when(handler.handle(resultSet, String.class)).thenReturn("result");

            Object result = actions.executeQuery(handler, String.class, ps);
            assertEquals("result", result);
            verify(resultSet).close();
        }

        @Test
        void executeUpdate_voidReturn_returnsVoidType() throws SQLException {
            when(ps.executeUpdate()).thenReturn(1);

            Object result = actions.executeUpdate(handler, void.class, ps, false, false, false);
            assertEquals(Void.TYPE, result);
        }

        @Test
        void executeUpdate_largeUpdateUnsupported_fallsBackToTheIntCount() throws SQLException {
            when(ps.executeLargeUpdate()).thenThrow(new java.sql.SQLFeatureNotSupportedException());
            when(ps.executeUpdate()).thenReturn(7);

            assertEquals(7L, actions.executeUpdate(handler, long.class, ps, false, false, true));
        }

        @Test
        void executeUpdate_largeUpdate_readsTheLongCount() throws SQLException {
            when(ps.executeLargeUpdate()).thenReturn(3_000_000_000L);

            Object result = actions.executeUpdate(handler, long.class, ps, false, false, true);

            assertEquals(3_000_000_000L, result);
            verify(ps, never()).executeUpdate();
        }

        @Test
        void executeUpdate_noReturnKeys_returnsCount() throws SQLException {
            when(ps.executeUpdate()).thenReturn(3);

            Object result = actions.executeUpdate(handler, int.class, ps, false, false, false);
            assertEquals(3, result);
        }

        @Test
        void executeUpdate_returnKeys_readsTheKeyColumn() throws SQLException {
            when(ps.executeUpdate()).thenReturn(1);
            when(ps.getGeneratedKeys()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getObject(1)).thenReturn(42L);

            Object result = actions.executeUpdate(handler, long.class, ps, true, false, false);

            assertEquals(42L, result);
            verify(resultSet).close();
        }

        @Test
        void executeUpdate_returnKeys_entityTypeStillUsesTheHandler() throws SQLException {
            java.lang.reflect.Type entityType = Object.class;
            when(ps.executeUpdate()).thenReturn(1);
            when(ps.getGeneratedKeys()).thenReturn(resultSet);
            when(handler.handle(resultSet, entityType)).thenReturn("mapped");

            Object result = actions.executeUpdate(handler, entityType, ps, true, false, false);

            assertEquals("mapped", result);
        }

        @Test
        void executeUpdate_optimisticLockFailed_throws() throws SQLException {
            when(ps.executeUpdate()).thenReturn(0);

            assertThrows(OptimisticLockException.class,
                    () -> actions.executeUpdate(handler, int.class, ps, false, true, false));
        }

        @Test
        void executeUpdate_optimisticLockSucceeded_returnsCount() throws SQLException {
            when(ps.executeUpdate()).thenReturn(1);

            Object result = actions.executeUpdate(handler, int.class, ps, false, true, false);
            assertEquals(1, result);
        }
    }

    @Nested
    class DmlOperationExecutorTest {

        @Mock PreparedStatementActions psActions;
        @Mock PreparedStatement ps;

        @Test
        void selectExecutor_supportedOperations() {
            DmlSelectOperationExecutor executor = new DmlSelectOperationExecutor(psActions);
            assertEquals(Set.of(DmlOperation.SELECT), executor.getSupportedOperations());
        }

        @Test
        void updateExecutor_supportedOperations() {
            DmlUpdateOperationExecutor executor = new DmlUpdateOperationExecutor(psActions);
            Set<DmlOperation> ops = executor.getSupportedOperations();
            assertTrue(ops.contains(DmlOperation.INSERT));
            assertTrue(ops.contains(DmlOperation.UPDATE));
            assertTrue(ops.contains(DmlOperation.DELETE));
            assertTrue(ops.contains(DmlOperation.MERGE));
        }

        @Test
        void selectExecutor_delegatesToActions() throws SQLException {
            when(psActions.executeQuery(any(), any(), any())).thenReturn("data");
            DmlSelectOperationExecutor executor = new DmlSelectOperationExecutor(psActions);

            QuerySpecifications specs = new QuerySpecifications();
            specs.setReturnType(String.class);
            specs.setHandler(mock(ResultSetHandler.class));

            Object result = executor.executeQuery(ps, specs);
            assertEquals("data", result);
        }
    }
}
