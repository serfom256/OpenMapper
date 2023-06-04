package com.openmapper.core.entity;


public class SQLToken {

    private final String data;

    private final int position;

    public SQLToken(String data, int position) {
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
