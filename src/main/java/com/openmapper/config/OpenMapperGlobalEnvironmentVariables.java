package com.openmapper.config;

import static com.openmapper.config.OpenMapperGlobalConstants.FSQL_FILES_PATH;
import static com.openmapper.config.OpenMapperGlobalConstants.LOGGING;
import static com.openmapper.config.OpenMapperGlobalConstants.PACKAGE_TO_SCAN;
import static com.openmapper.config.OpenMapperGlobalConstants.QUERY_CACHE_ENABLED;
import static com.openmapper.config.OpenMapperGlobalConstants.RESULT_CACHE_ENABLED;
import static com.openmapper.config.OpenMapperGlobalConstants.SQL_TRACING;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class OpenMapperGlobalEnvironmentVariables {

    private final Environment environment;

    public OpenMapperGlobalEnvironmentVariables(Environment environment) {
        this.environment = environment;
    }

    private boolean isLogging;
    private boolean isSqlTracing;
    private boolean isQueryCacheEnabled;
    private boolean isResultCacheEnabled;
    private String packageToScan;
    private List<String> sqlFilePath;

    @PostConstruct
    private void initVariables() {
        isLogging = getBoolean(LOGGING.value());
        isSqlTracing = getBoolean(SQL_TRACING.value());
        sqlFilePath = getList(FSQL_FILES_PATH.value());
        packageToScan = getString(PACKAGE_TO_SCAN.value());
        isQueryCacheEnabled = getBoolean(QUERY_CACHE_ENABLED.value());
        isResultCacheEnabled = getBoolean(RESULT_CACHE_ENABLED.value());
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

    public String getPackageToScan() {
        return packageToScan;
    }

    public boolean isLogging() {
        return isLogging;
    }

    public boolean isSqlTracing() {
        return isSqlTracing;
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
