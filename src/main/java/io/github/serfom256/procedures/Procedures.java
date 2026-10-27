package io.github.serfom256.procedures;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github.serfom256.model.operations.DmlOperation;

/**
 * Runs a statement written in place, without declaring it in a {@code .fsql} file or on
 * a {@code @DaoLayer} interface. Inject it like any other bean.
 *
 * <pre>{@code
 * @Service
 * public class ReportService {
 *
 *     private final Procedures procedures;
 *
 *     public List<Product> expensive(BigDecimal from) {
 *         return procedures.select(
 *                 "SELECT id, name, price FROM product WHERE price > [from] ORDER BY id",
 *                 Product.class, Map.of("from", from));
 *     }
 * }
 * }</pre>
 *
 * <p>Statements use the same {@code [name]} placeholders as a {@code .fsql} procedure and
 * are bound as JDBC parameters, never concatenated. Results map exactly as they do for a
 * DAO method: a {@code @Model} type is built as a full object graph, {@code @Joined}
 * associations included, and anything else is read as a scalar. Statements join the
 * caller's transaction.
 *
 * <p>Defaults such as the fetch size, the query timeout and the data source are set by
 * declaring a {@link ProceduresCustomizer} bean.
 *
 * <p>What a DAO interface gives you and this does not: the statement is parsed on first
 * use rather than validated at startup, and the SQL lives at the call site instead of in
 * one file. Prefer a DAO method for anything an application runs routinely.
 */
public interface Procedures {

    /**
     * @param sql statement with {@code [name]} placeholders
     * @param type {@code @Model} type to build, or a scalar type
     * @param parameters values by placeholder name
     * @return every row, empty when none matched
     */
    <T> List<T> select(String sql, Class<T> type, Map<String, Object> parameters);

    /**
     * @return every row of a statement that takes no parameters
     */
    <T> List<T> select(String sql, Class<T> type);

    /**
     * @return the single row, empty when none matched
     * @throws io.github.serfom256.exceptions.internal.TooManyResultsFoundException if a
     *         scalar query matched more than one row
     */
    <T> Optional<T> selectOne(String sql, Class<T> type, Map<String, Object> parameters);

    /**
     * @return the single row of a statement that takes no parameters, empty when none matched
     */
    <T> Optional<T> selectOne(String sql, Class<T> type);

    /**
     * @param operation the statement being run, so inserts and updates can differ
     * @return the number of rows the statement changed, read through
     *         {@link java.sql.PreparedStatement#executeLargeUpdate()} where the driver
     *         supports it, so a count beyond {@code Integer.MAX_VALUE} survives
     */
    long execute(String sql, DmlOperation operation, Map<String, Object> parameters);

    /**
     * @param keyType type of the key the database generates
     * @return the generated key, empty when the statement produced none
     */
    <K> Optional<K> executeReturningKey(String sql, DmlOperation operation, Class<K> keyType,
                                        Map<String, Object> parameters);

    /**
     * Reads the generated key from a named column.
     *
     * <p>Oracle reports a {@code ROWID} rather than the generated value unless the column is
     * named, so a statement that has to run on Oracle should name it here. A DAO method takes
     * the column from its {@code @Model} and needs no equivalent.
     *
     * @param keyColumn column the database generates, as spelled in the table
     * @return the generated key, empty when the statement produced none
     */
    <K> Optional<K> executeReturningKey(String sql, DmlOperation operation, Class<K> keyType,
                                        Map<String, Object> parameters, String keyColumn);
}
