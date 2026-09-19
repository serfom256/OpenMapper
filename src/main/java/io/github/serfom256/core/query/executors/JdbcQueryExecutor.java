package io.github.serfom256.core.query.executors;

import io.github.serfom256.core.query.executors.operations.DatabaseOperation;
import io.github.serfom256.core.query.model.BoundSql;
import io.github.serfom256.core.query.mapping.ResultSetGraphReader;
import io.github.serfom256.core.query.model.QuerySpecifications;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLExceptionSubclassTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.UncategorizedSQLException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Stream;

public class JdbcQueryExecutor implements QueryExecutor {

    private final QueryExecutionStrategy queryExecutionStrategy;
    private final SQLExceptionTranslator exceptionTranslator = new SQLExceptionSubclassTranslator();
    private final ResultSetGraphReader graphReader;

    public JdbcQueryExecutor(QueryExecutionStrategy queryExecutionStrategy,
                             ResultSetGraphReader graphReader) {
        this.queryExecutionStrategy = queryExecutionStrategy;
        this.graphReader = graphReader;
    }

    @Override
    public Object execute(BoundSql boundSql, QuerySpecifications querySpecifications) {
        if (querySpecifications.isStreaming()) {
            return executeStream(boundSql, querySpecifications);
        }
        DataSource dataSource = querySpecifications.getDataSource();
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement preparedStatement =
                     prepare(connection, boundSql.getSql(), querySpecifications)) {

            applyStatementOptions(preparedStatement, querySpecifications);
            bindParameters(preparedStatement, boundSql.getParameters());

            DatabaseOperation dbOps = queryExecutionStrategy.getOperationByType(querySpecifications.getOperation());
            return dbOps.executeQuery(preparedStatement, querySpecifications);
        } catch (SQLException e) {
            throw translate(boundSql, e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    public Object executeBatch(List<BoundSql> statements, QuerySpecifications querySpecifications) {
        if (statements.isEmpty()) {
            return 0;
        }
        BoundSql first = statements.get(0);
        DataSource dataSource = querySpecifications.getDataSource();
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement preparedStatement =
                     prepare(connection, first.getSql(), querySpecifications)) {
            applyStatementOptions(preparedStatement, querySpecifications);
            for (BoundSql statement : statements) {
                bindParameters(preparedStatement, statement.getParameters());
                preparedStatement.addBatch();
            }
            return queryExecutionStrategy.getOperationByType(querySpecifications.getOperation())
                    .executeBatch(preparedStatement, querySpecifications);
        } catch (SQLException e) {
            throw translate(first, e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private PreparedStatement prepare(Connection connection, String sql, QuerySpecifications querySpecifications)
            throws SQLException {
        if (querySpecifications.shouldReturnGeneratedKeys()) {
            return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        }
        return connection.prepareStatement(sql);
    }

    private DataAccessException translate(BoundSql boundSql, SQLException e) {
        DataAccessException translated =
                exceptionTranslator.translate("openMapper query", boundSql.getSql(), e);
        return translated != null
                ? translated
                : new UncategorizedSQLException("openMapper query", boundSql.getSql(), e);
    }

    private Object executeStream(BoundSql boundSql, QuerySpecifications querySpecifications) {
        DataSource dataSource = querySpecifications.getDataSource();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(boundSql.getSql());
            applyStatementOptions(preparedStatement, querySpecifications);
            bindParameters(preparedStatement, boundSql.getParameters());
            resultSet = preparedStatement.executeQuery();

            Class<?> elementType = (Class<?>) querySpecifications.getReturnType();
            Stream<Object> stream = graphReader.stream(elementType, resultSet);

            ResultSet openResultSet = resultSet;
            PreparedStatement openStatement = preparedStatement;
            return stream.onClose(() -> release(openResultSet, openStatement, connection, dataSource));
        } catch (RuntimeException | SQLException e) {
            release(resultSet, preparedStatement, connection, dataSource);
            throw e instanceof SQLException sqlException
                    ? translate(boundSql, sqlException)
                    : (RuntimeException) e;
        }
    }

    private void release(ResultSet resultSet, PreparedStatement preparedStatement,
                         Connection connection, DataSource dataSource) {
        closeQuietly(resultSet);
        closeQuietly(preparedStatement);
        DataSourceUtils.releaseConnection(connection, dataSource);
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }

    private void applyStatementOptions(PreparedStatement preparedStatement,
                                       QuerySpecifications querySpecifications) throws SQLException {
        if (querySpecifications.getFetchSize() > 0) {
            preparedStatement.setFetchSize(querySpecifications.getFetchSize());
        }
        if (querySpecifications.getQueryTimeoutSeconds() > 0) {
            preparedStatement.setQueryTimeout(querySpecifications.getQueryTimeoutSeconds());
        }
    }

    private void bindParameters(PreparedStatement preparedStatement, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            preparedStatement.setObject(i + 1, parameters.get(i));
        }
    }
}
