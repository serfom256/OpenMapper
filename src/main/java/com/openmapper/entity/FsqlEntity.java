package com.openmapper.entity;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FsqlEntity {

    private final List<String> sql;

    private final Set<String> variables;

    public FsqlEntity(List<String> sql, Set<String> variables) {
        this.sql = Collections.unmodifiableList(sql);
        this.variables = Collections.unmodifiableSet(variables);
    }

    public List<String> getSql() {
        return sql;
    }

    public Set<String> getVariables() {
        return variables;
    }
}
