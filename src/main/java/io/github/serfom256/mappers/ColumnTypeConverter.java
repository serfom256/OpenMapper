package io.github.serfom256.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps one Java type to and from a database column.
 *
 * <p>Define a converter as a Spring bean to support a type openMapper does not
 * know: a value object, a domain-specific identifier, a column whose stored form
 * differs from its Java form. Converters take precedence over the built-in
 * types, so an application can also override how a known type is read.
 *
 * <pre>{@code
 * @Component
 * public class MoneyConverter implements ColumnTypeConverter<Money> {
 *
 *     public Class<Money> type() {
 *         return Money.class;
 *     }
 *
 *     public Money fromColumn(ResultSet rs, String column) throws SQLException {
 *         BigDecimal amount = rs.getBigDecimal(column);
 *         return amount == null ? null : new Money(amount);
 *     }
 *
 *     public Object toParameter(Money value) {
 *         return value == null ? null : value.amount();
 *     }
 * }
 * }</pre>
 */
public interface ColumnTypeConverter<T> {

    /** The Java type this converter handles. */
    Class<T> type();

    /** Reads the column for the current row, returning {@code null} for SQL NULL. */
    T fromColumn(ResultSet resultSet, String column) throws SQLException;

    /**
     * Converts a value to the form bound as a statement parameter. The default
     * binds the value unchanged, which suits any type the JDBC driver already
     * understands.
     */
    default Object toParameter(T value) {
        return value;
    }
}
