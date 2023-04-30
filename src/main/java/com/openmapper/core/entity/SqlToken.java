package com.openmapper.core.entity;


public class SqlToken {

    private final String data;

    private final int position;

    public SqlToken(String data, int position) {
        this.data = data;
        this.position = position;
    }

    public String getData() {
        return data;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return data;
    }

}
