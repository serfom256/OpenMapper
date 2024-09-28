package com.openmapper.parser.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SQLRecord {

    private final List<SQLToken> sql;

    private final Map<String, SQLToken> variables;

    public SQLRecord(List<SQLToken> sql, Map<String, SQLToken> sequence) {
        this.sql = Collections.unmodifiableList(sql);
        this.variables = sequence;
    }

    public List<SQLToken> getSql() {
        return sql;
    }


    public Map<String, SQLToken> getVariables() {
        return variables;
    }

    @Override
    public String toString() {
        return "SqlRecord{" +
                "sql=" + sql +
                ", variables=" + variables +
                '}';
    }
}
