package io.github.serfom256.unit.core.query.executors;

import io.github.serfom256.core.query.executors.JdbcQueryExecutor;
import io.github.serfom256.core.query.mapping.MappingPlanRegistry;
import io.github.serfom256.core.query.mapping.ResultSetGraphReader;
import io.github.serfom256.core.query.executors.QueryExecutionStrategy;
import io.github.serfom256.core.query.executors.operations.DatabaseOperation;
import io.github.serfom256.core.query.model.BoundSql;
import io.github.serfom256.core.query.model.QuerySpecifications;
import org.springframework.dao.DataAccessException;
import io.github.serfom256.model.operations.DmlOperation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JdbcQueryExecutorTest {

    private final ResultSetGraphReader graphReader =
            new ResultSetGraphReader(new MappingPlanRegistry(null));

    @Mock QueryExecutionStrategy strategy;
    @Mock DataSource dataSource;
    @Mock Connection connection;
    @Mock PreparedStatement preparedStatement;
    @Mock DatabaseOperation dbOperation;

    @Test
    void execute_success() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(strategy.getOperationByType(DmlOperation.SELECT)).thenReturn(dbOperation);
        when(dbOperation.executeQuery(any(), any())).thenReturn("result");

        JdbcQueryExecutor executor = new JdbcQueryExecutor(strategy, graphReader);
        QuerySpecifications specs = new QuerySpecifications();
        specs.setDataSource(dataSource);
        specs.setOperation(DmlOperation.SELECT);
        specs.setShouldReturnGeneratedKeys(false);

        Object result = executor.execute(new BoundSql("SELECT 1", List.of()), specs);
        assertEquals("result", result);
    }

    @Test
    void execute_bindsParametersInOrder() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(strategy.getOperationByType(any())).thenReturn(dbOperation);
        when(dbOperation.executeQuery(any(), any())).thenReturn(null);

        JdbcQueryExecutor executor = new JdbcQueryExecutor(strategy, graphReader);
        QuerySpecifications specs = new QuerySpecifications();
        specs.setDataSource(dataSource);
        specs.setOperation(DmlOperation.SELECT);

        executor.execute(new BoundSql("SELECT * FROM t WHERE a = ? AND b = ?", List.of("x' OR '1'='1", 42)), specs);

        // values are bound, never concatenated into the SQL text
        verify(preparedStatement).setObject(1, "x' OR '1'='1");
        verify(preparedStatement).setObject(2, 42);
    }

    @Test
    void execute_withReturnGeneratedKeys() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("INSERT INTO t", Statement.RETURN_GENERATED_KEYS))
                .thenReturn(preparedStatement);
        when(strategy.getOperationByType(DmlOperation.INSERT)).thenReturn(dbOperation);
        when(dbOperation.executeQuery(any(), any())).thenReturn(99L);

        JdbcQueryExecutor executor = new JdbcQueryExecutor(strategy, graphReader);
        QuerySpecifications specs = new QuerySpecifications();
        specs.setDataSource(dataSource);
        specs.setOperation(DmlOperation.INSERT);
        specs.setShouldReturnGeneratedKeys(true);

        Object result = executor.execute(new BoundSql("INSERT INTO t", List.of()), specs);
        assertEquals(99L, result);
    }

    @Test
    void execute_withoutGeneratedKeys_usesThePlainOverload() throws SQLException {
        // Passing 0 to prepareStatement(String, int) is not a JDBC constant: HSQLDB
        // rejects it outright, so the no-keys path must use the single-argument form.
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT 1")).thenReturn(preparedStatement);
        when(strategy.getOperationByType(DmlOperation.SELECT)).thenReturn(dbOperation);
        when(dbOperation.executeQuery(any(), any())).thenReturn("ok");

        JdbcQueryExecutor executor = new JdbcQueryExecutor(strategy, graphReader);
        QuerySpecifications specs = new QuerySpecifications();
        specs.setDataSource(dataSource);
        specs.setOperation(DmlOperation.SELECT);

        assertEquals("ok", executor.execute(new BoundSql("SELECT 1", List.of()), specs));
        verify(connection).prepareStatement("SELECT 1");
        verify(connection, never()).prepareStatement(anyString(), anyInt());
    }

    @Test
    void execute_sqlException_throws() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenThrow(new SQLException("fail"));

        JdbcQueryExecutor executor = new JdbcQueryExecutor(strategy, graphReader);
        QuerySpecifications specs = new QuerySpecifications();
        specs.setDataSource(dataSource);
        specs.setOperation(DmlOperation.SELECT);

        assertThrows(DataAccessException.class,
                () -> executor.execute(new BoundSql("SELECT 1", List.of()), specs));
    }

    @Test
    void execute_closesResources() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(strategy.getOperationByType(any())).thenReturn(dbOperation);
        when(dbOperation.executeQuery(any(), any())).thenReturn(null);

        JdbcQueryExecutor executor = new JdbcQueryExecutor(strategy, graphReader);
        QuerySpecifications specs = new QuerySpecifications();
        specs.setDataSource(dataSource);
        specs.setOperation(DmlOperation.SELECT);

        executor.execute(new BoundSql("SELECT 1", List.of()), specs);

        verify(preparedStatement).close();
        verify(connection).close();
    }
}
