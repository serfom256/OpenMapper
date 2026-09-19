package io.github.serfom256.core.query.model;

import java.util.List;

public final class BoundSql {

    private final String sql;
    private final List<Object> parameters;

    public BoundSql(String sql, List<Object> parameters) {
        this.sql = sql;
        this.parameters = parameters;
    }

    public String getSql() {
        return sql;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return sql + " " + parameters;
    }
}
