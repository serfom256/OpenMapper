package com.openmapper.config;

public enum OpenMapperGlobalConstants {

    FILE_EXTENSION(".fsql"),
    FSQL_FILES_PATH("openmapper.fsql.path"),
    DAO_PACKAGE_TO_SCAN("openmapper.dao.packagesToScan"),
    MODEL_PACKAGE_TO_SCAN("openmapper.model.packagesToScan"),
    SQL_TRACING("opnemapper.sql.tracing"),
    LOGGING("opnemapper.logging"),
    QUERY_CACHE_ENABLED("openmapper.query.cache.enabled"),
    RESULT_CACHE_ENABLED("openmapper.cache.enabled");

    private final String value;

    OpenMapperGlobalConstants(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
