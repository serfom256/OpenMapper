package com.openmapper.config;

public enum OPEN_MAPPER_CONSTANTS {

    FILE_EXTENSION(".fsql"),
    FSQL_FILES_PATH("openmapper.fsql.files"),
    PACKAGE_TO_SCAN("openmapper.packagesToScan"),
    SQL_TRACING("opnemapper.sql.tracing"),

    LOGGING("opnemapper.logging");

    private final String value;

    OPEN_MAPPER_CONSTANTS(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
