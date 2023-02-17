package com.openmapper.config;

public enum OPEN_MAPPER_CONSTANTS {

    FILE_EXTENSION(".fsql"),
    VARIABLE_FORMAT("`%s`"),
    FSQL_FILES_PATH("openmapper.fsql.files"),
    PACKAGE_TO_SCAN("openmapper.packagesToScan");

    private final String value;

    OPEN_MAPPER_CONSTANTS(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
