package io.github.serfom256.benchmark;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Map;

/**
 * Minimal in-memory {@link ResultSet} over a single row, for benchmarks that
 * measure mapping rather than the database.
 *
 * <p>A mock framework is unusable here: stubbing costs far more than the mapping
 * being measured, and re-stubbing inside a benchmark method swamps the result
 * entirely. A dynamic proxy adds a constant that is identical for every mapping
 * strategy, so comparisons between them stay meaningful.
 */
public final class FakeResultSet {

    private FakeResultSet() {
    }

    public static final class Cursor {
        private final Map<String, Object> row;
        private final String[] columns;
        private boolean consumed;
        final ResultSet resultSet;

        Cursor(Map<String, Object> row) {
            this.row = row;
            this.columns = row.keySet().toArray(new String[0]);
            this.resultSet = (ResultSet) Proxy.newProxyInstance(
                    ResultSet.class.getClassLoader(),
                    new Class<?>[]{ResultSet.class},
                    this::handle);
        }

        public ResultSet rewind() {
            consumed = false;
            return resultSet;
        }

        private Object handle(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "next":
                    if (consumed) {
                        return false;
                    }
                    consumed = true;
                    return true;
                case "getMetaData":
                    return metaData();
                case "wasNull":
                    return false;
                case "close":
                    return null;
                default:
                    return value(method, args);
            }
        }

        private Object value(Method method, Object[] args) {
            Object raw = args != null && args.length == 1 && args[0] instanceof String
                    ? row.get(args[0])
                    : null;
            return raw != null ? raw : defaultFor(method.getReturnType());
        }

        private ResultSetMetaData metaData() {
            return (ResultSetMetaData) Proxy.newProxyInstance(
                    ResultSetMetaData.class.getClassLoader(),
                    new Class<?>[]{ResultSetMetaData.class},
                    (p, m, a) -> switch (m.getName()) {
                        case "getColumnCount" -> columns.length;
                        case "getColumnLabel", "getColumnName" -> columns[(int) a[0] - 1];
                        default -> defaultFor(m.getReturnType());
                    });
        }

        private static Object defaultFor(Class<?> returnType) {
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == long.class) {
                return 0L;
            }
            if (returnType == double.class) {
                return 0d;
            }
            if (returnType == float.class) {
                return 0f;
            }
            return 0;
        }
    }

    public static Cursor singleRow(Map<String, Object> row) {
        return new Cursor(row);
    }
}
