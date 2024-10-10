package com.openmapper.config;

import static com.openmapper.config.OpenMapperGlobalConstants.FSQL_FILES_PATH;
import static com.openmapper.config.OpenMapperGlobalConstants.INPUT_WRAPPING_ENABLED;
import static com.openmapper.config.OpenMapperGlobalConstants.LOGGING_ENABLED;
import static com.openmapper.config.OpenMapperGlobalConstants.MODEL_PACKAGE_TO_SCAN;
import static com.openmapper.config.OpenMapperGlobalConstants.DAO_PACKAGE_TO_SCAN;
import static com.openmapper.config.OpenMapperGlobalConstants.QUERY_CACHE_ENABLED;
import static com.openmapper.config.OpenMapperGlobalConstants.RESULT_CACHE_ENABLED;
import static com.openmapper.config.OpenMapperGlobalConstants.SQL_TRACING_ENABLED;
import static com.openmapper.config.OpenMapperGlobalConstants.SQL_TRACING_QUERIES_ENABLED;

import java.util.List;

import org.springframework.core.env.Environment;

public class OpenMapperGlobalEnvironmentVariables {

    private final Environment environment;

    private boolean isLoggingEnabled;
    private boolean isSqlTracingEnabled;
    private boolean isSqlQueriesTracingEnabled;
    private boolean isQueryCacheEnabled;
    private boolean isResultCacheEnabled;
    private boolean isInputWrappingEnabled;
    private String daoPackageToScan;
    private String modelPackageToScan;
    private List<String> sqlFilePath;

    public OpenMapperGlobalEnvironmentVariables(Environment environment) {
        this.environment = environment;
        initVariables();
    }

    private void initVariables() {
        isLoggingEnabled = getBoolean(LOGGING_ENABLED.value());
        isSqlTracingEnabled = getBoolean(SQL_TRACING_ENABLED.value());
        isSqlQueriesTracingEnabled = getBoolean(SQL_TRACING_QUERIES_ENABLED.value());
        sqlFilePath = getList(FSQL_FILES_PATH.value());
        daoPackageToScan = getString(DAO_PACKAGE_TO_SCAN.value());
        modelPackageToScan = getString(MODEL_PACKAGE_TO_SCAN.value());
        isQueryCacheEnabled = getBoolean(QUERY_CACHE_ENABLED.value());
        isResultCacheEnabled = getBoolean(RESULT_CACHE_ENABLED.value());
        isInputWrappingEnabled = getBoolean(INPUT_WRAPPING_ENABLED.value());
    }

    public boolean isQueryCacheEnabled() {
        return isQueryCacheEnabled;
    }

    public boolean isResultCacheEnabled() {
        return isResultCacheEnabled;
    }

    public List<String> getSqlFilePaths() {
        return sqlFilePath;
    }

    public String getDaoPackageToScan() {
        return daoPackageToScan;
    }

    public String getModelPackageToScan() {
        return modelPackageToScan;
    }

    public boolean isLoggingEnabled() {
        return isLoggingEnabled;
    }

    public boolean isSqlTracingEnabled() {
        return isSqlTracingEnabled;
    }
    public boolean isSqlQueryTracingEnabled() {
        return isSqlQueriesTracingEnabled;
    }

    public boolean isWrappingEnabled() {
        return isInputWrappingEnabled;
    }

    private boolean getBoolean(String propertyName) {
        String booleanString = environment.getProperty(propertyName);
        return Boolean.parseBoolean(booleanString == null ? "false" : booleanString);
    }

    @SuppressWarnings("unchecked")
    private List<String> getList(String propertyName) {
        return environment.getProperty(propertyName, List.class);
    }

    private String getString(String propertyName) {
        return environment.getProperty(propertyName);
    }
}
