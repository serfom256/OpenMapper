package com.openmapper.config;

public enum OPEN_MAPPER_CONSTANTS {

    FILE_EXTENSION(".fsql"),
    VARIABLE_FORMAT("`%s`");

    private final String value;

    OPEN_MAPPER_CONSTANTS(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
