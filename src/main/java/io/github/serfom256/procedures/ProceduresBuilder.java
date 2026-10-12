package io.github.serfom256.procedures;

import java.util.function.Supplier;

import javax.sql.DataSource;

import io.github.serfom256.core.query.executors.QueryExecutor;
import io.github.serfom256.core.query.handlers.ResultSetHandler;
import io.github.serfom256.core.query.source.mapping.InputMapper;
import io.github.serfom256.parser.SourceMapper;

/**
 * Builds the {@link Procedures} instance. openMapper creates one, hands it to
 * every
 * {@link ProceduresCustomizer} bean, and then calls {@link #build()}; reach it
 * through a
 * customizer rather than constructing it yourself.
 */
public final class ProceduresBuilder {

    private final SourceMapper sourceMapper;
    private final InputMapper inputMapper;
    private final QueryExecutor queryExecutor;
    private final ResultSetHandler<?> entityHandler;
    private final ResultSetHandler<?> iterableHandler;

    private Supplier<DataSource> dataSource;
    private int fetchSize;
    private int queryTimeoutSeconds;
    private int statementCacheSize = 512;

    public ProceduresBuilder(SourceMapper sourceMapper,
            InputMapper inputMapper,
            QueryExecutor queryExecutor,
            ResultSetHandler<?> entityHandler,
            ResultSetHandler<?> iterableHandler,
            Supplier<DataSource> dataSource) {
        this.sourceMapper = sourceMapper;
        this.inputMapper = inputMapper;
        this.queryExecutor = queryExecutor;
        this.entityHandler = entityHandler;
        this.iterableHandler = iterableHandler;
        this.dataSource = dataSource;
    }

    /**
     * Runs statements against this data source instead of the application's primary
     * one.
     */
    public ProceduresBuilder dataSource(DataSource dataSource) {
        this.dataSource = () -> dataSource;
        return this;
    }

    /**
     * @param fetchSize rows the driver fetches per round trip, {@code 0} for its
     *                  default
     */
    public ProceduresBuilder fetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
        return this;
    }

    /**
     * @param queryTimeoutSeconds seconds a statement may run, {@code 0} for no
     *                            limit
     */
    public ProceduresBuilder queryTimeoutSeconds(int queryTimeoutSeconds) {
        this.queryTimeoutSeconds = queryTimeoutSeconds;
        return this;
    }

    /**
     * @param statementCacheSize how many parsed statements to keep, {@code 0} to
     *                           parse
     *                           every call
     */
    public ProceduresBuilder statementCacheSize(int statementCacheSize) {
        this.statementCacheSize = statementCacheSize;
        return this;
    }

    public Procedures build() {
        return new ProceduresImpl(sourceMapper, inputMapper, queryExecutor, entityHandler,
                iterableHandler, dataSource, fetchSize, queryTimeoutSeconds, statementCacheSize);
    }
}
