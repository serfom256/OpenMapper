package io.github.serfom256.core.query.model;

import java.util.List;

/**
 * A SQL statement that uses {@code ?} placeholders together with the ordered
 * list of values to bind to them. Values are never concatenated into the SQL
 * text; they are bound through {@link java.sql.PreparedStatement#setObject},
 * which is what makes the query safe from SQL injection.
 */
public final class BoundSql {

    private final String sql;
    private final List<Object> parameters;

    public BoundSql(String sql, List<Object> parameters) {
        this.sql = sql;
        this.parameters = parameters;
    }

    /** The SQL text with one {@code ?} per bound parameter. */
    public String getSql() {
        return sql;
    }

    /** The parameter values in placeholder order. */
    public List<Object> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return sql + " " + parameters;
    }
}
