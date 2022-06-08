package com.openmapper.parser.entity;

public class SqlTemporaryEntity {

    private String name;
    private String sql;


    public SqlTemporaryEntity(String name, String sql) {
        this.name = name;
        this.sql = sql;
    }

    public SqlTemporaryEntity() {
    }

    public String getName() {
        return name;
    }

    public String getSql() {
        return sql;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public boolean isValid() {
        return sql != null && name != null;
    }
}

