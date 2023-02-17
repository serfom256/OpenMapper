package com.openmapper.entity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FsqlEntity {

    private final List<SqlToken> sql;

    private final Map<String, SqlToken> variables;

    public FsqlEntity(List<SqlToken> sql, Map<String, SqlToken> sequence) {
        this.sql = Collections.unmodifiableList(sql);
        this.variables = sequence;
    }

    public List<SqlToken> getSql() {
        return sql;
    }


    public Map<String, SqlToken> getVariables() {
        return variables;
    }

    @Override
    public String toString() {
        return "FsqlEntity{" +
                "sql=" + sql +
                ", variables=" + variables +
                '}';
    }
}
