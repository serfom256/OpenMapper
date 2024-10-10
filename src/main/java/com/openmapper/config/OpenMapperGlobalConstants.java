package com.openmapper.config;

public enum OpenMapperGlobalConstants {

    FILE_EXTENSION(".fsql"),
    FSQL_FILES_PATH("openmapper.fsql.path"),
    DAO_PACKAGE_TO_SCAN("openmapper.dao.packagesToScan"),
    MODEL_PACKAGE_TO_SCAN("openmapper.model.packagesToScan"),
    SQL_TRACING_ENABLED("openmapper.sql.tracing.enabled"),
    SQL_TRACING_QUERIES_ENABLED("openmapper.sql.tracing.queries.enabled"),
    LOGGING_ENABLED("openmapper.logging.enabled"),
    QUERY_CACHE_ENABLED("openmapper.query.cache.enabled"),
    RESULT_CACHE_ENABLED("openmapper.cache.enabled"),
    INPUT_WRAPPING_ENABLED("openmapper.input.wrapping.enabled");

    private final String value;

    OpenMapperGlobalConstants(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
