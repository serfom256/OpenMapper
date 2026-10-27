package io.github.serfom256.procedures;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.sql.DataSource;

import io.github.serfom256.core.query.executors.QueryExecutor;
import io.github.serfom256.core.query.handlers.ResultSetHandler;
import io.github.serfom256.core.query.model.BoundSql;
import io.github.serfom256.core.query.model.QuerySpecifications;
import io.github.serfom256.core.query.source.mapping.InputMapper;
import io.github.serfom256.model.operations.DmlOperation;
import io.github.serfom256.parser.SourceMapper;
import io.github.serfom256.parser.model.SQLRecord;

class ProceduresImpl implements Procedures {

    private final Map<String, SQLRecord> parsed = new ConcurrentHashMap<>();

    private final SourceMapper sourceMapper;
    private final InputMapper inputMapper;
    private final QueryExecutor queryExecutor;
    private final ResultSetHandler<?> entityHandler;
    private final ResultSetHandler<?> iterableHandler;
    private final Supplier<DataSource> dataSource;
    private final int fetchSize;
    private final int queryTimeoutSeconds;
    private final int statementCacheSize;

    ProceduresImpl(SourceMapper sourceMapper,
                   InputMapper inputMapper,
                   QueryExecutor queryExecutor,
                   ResultSetHandler<?> entityHandler,
                   ResultSetHandler<?> iterableHandler,
                   Supplier<DataSource> dataSource,
                   int fetchSize,
                   int queryTimeoutSeconds,
                   int statementCacheSize) {
        this.sourceMapper = sourceMapper;
        this.inputMapper = inputMapper;
        this.queryExecutor = queryExecutor;
        this.entityHandler = entityHandler;
        this.iterableHandler = iterableHandler;
        this.dataSource = dataSource;
        this.fetchSize = fetchSize;
        this.queryTimeoutSeconds = queryTimeoutSeconds;
        this.statementCacheSize = statementCacheSize;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> select(String sql, Class<T> type, Map<String, Object> parameters) {
        QuerySpecifications specifications = specifications(DmlOperation.SELECT, parameters);
        specifications.setHandler(iterableHandler);
        specifications.setReturnType(listOf(type));
        Object result = queryExecutor.execute(bind(sql, parameters), specifications);
        return result == null ? new ArrayList<>() : new ArrayList<>((List<T>) result);
    }

    @Override
    public <T> List<T> select(String sql, Class<T> type) {
        return select(sql, type, Map.of());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> selectOne(String sql, Class<T> type, Map<String, Object> parameters) {
        QuerySpecifications specifications = specifications(DmlOperation.SELECT, parameters);
        specifications.setHandler(entityHandler);
        specifications.setReturnType(type);
        return Optional.ofNullable((T) queryExecutor.execute(bind(sql, parameters), specifications));
    }

    @Override
    public <T> Optional<T> selectOne(String sql, Class<T> type) {
        return selectOne(sql, type, Map.of());
    }

    @Override
    public long execute(String sql, DmlOperation operation, Map<String, Object> parameters) {
        QuerySpecifications specifications = specifications(operation, parameters);
        specifications.setHandler(entityHandler);
        specifications.setReturnType(long.class);
        specifications.setLargeUpdate(true);
        return (long) queryExecutor.execute(bind(sql, parameters), specifications);
    }

    @Override
    public <K> Optional<K> executeReturningKey(String sql, DmlOperation operation, Class<K> keyType,
                                               Map<String, Object> parameters) {
        return executeReturningKey(sql, operation, keyType, parameters, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K> Optional<K> executeReturningKey(String sql, DmlOperation operation, Class<K> keyType,
                                               Map<String, Object> parameters, String keyColumn) {
        QuerySpecifications specifications = specifications(operation, parameters);
        specifications.setHandler(entityHandler);
        specifications.setReturnType(keyType);
        specifications.setShouldReturnGeneratedKeys(true);
        if (keyColumn != null && !keyColumn.isBlank()) {
            specifications.setGeneratedKeyColumns(new String[]{keyColumn});
        }
        return Optional.ofNullable((K) queryExecutor.execute(bind(sql, parameters), specifications));
    }

    private QuerySpecifications specifications(DmlOperation operation, Map<String, Object> parameters) {
        QuerySpecifications specifications = new QuerySpecifications();
        specifications.setOperation(operation);
        specifications.setDataSource(dataSource.get());
        specifications.setParams(parameters);
        specifications.setFetchSize(fetchSize);
        specifications.setQueryTimeoutSeconds(queryTimeoutSeconds);
        return specifications;
    }

    private BoundSql bind(String sql, Map<String, Object> parameters) {
        return inputMapper.mapSql(record(sql), parameters);
    }

    private SQLRecord record(String sql) {
        if (statementCacheSize <= 0) {
            return sourceMapper.map(sql);
        }
        SQLRecord cached = parsed.get(sql);
        if (cached != null) {
            return cached;
        }
        SQLRecord mapped = sourceMapper.map(sql);
        if (parsed.size() >= statementCacheSize) {
            parsed.clear();
        }
        parsed.put(sql, mapped);
        return mapped;
    }

    private Type listOf(Class<?> element) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{element};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }
}
