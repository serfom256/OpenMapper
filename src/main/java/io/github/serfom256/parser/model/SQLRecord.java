package io.github.serfom256.parser.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLRecord {

    private final List<SQLToken> sql;

    private final Map<String, SQLToken> variables;

    private final Map<Integer, String> variablePositions;

    public SQLRecord(List<SQLToken> sql, Map<String, SQLToken> sequence) {
        this(sql, sequence, positionsOf(sequence));
    }

    public SQLRecord(List<SQLToken> sql, Map<String, SQLToken> sequence,
                     Map<Integer, String> variablePositions) {
        this.sql = Collections.unmodifiableList(sql);
        this.variables = sequence;
        this.variablePositions = Collections.unmodifiableMap(variablePositions);
    }

    private static Map<Integer, String> positionsOf(Map<String, SQLToken> sequence) {
        Map<Integer, String> positions = new HashMap<>();
        sequence.forEach((name, token) -> positions.put(token.getPosition(), name));
        return positions;
    }

    public Map<Integer, String> getVariablePositions() {
        return variablePositions;
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
