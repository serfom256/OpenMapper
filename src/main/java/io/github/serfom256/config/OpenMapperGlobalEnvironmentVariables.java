package io.github.serfom256.config;

import static io.github.serfom256.config.OpenMapperGlobalConstants.FSQL_FILES_PATH;
import static io.github.serfom256.config.OpenMapperGlobalConstants.IN_CLAUSE_CHUNK_SIZE;
import static io.github.serfom256.config.OpenMapperGlobalConstants.LOGGING_ENABLED;
import static io.github.serfom256.config.OpenMapperGlobalConstants.MODEL_PACKAGE_TO_SCAN;
import static io.github.serfom256.config.OpenMapperGlobalConstants.DAO_PACKAGE_TO_SCAN;
import static io.github.serfom256.config.OpenMapperGlobalConstants.QUERY_CACHE_ENABLED;
import static io.github.serfom256.config.OpenMapperGlobalConstants.SQL_TRACING_ENABLED;
import static io.github.serfom256.config.OpenMapperGlobalConstants.SQL_TRACING_QUERIES_ENABLED;

import java.util.List;

import org.springframework.core.env.Environment;

public class OpenMapperGlobalEnvironmentVariables {

    private static final int DEFAULT_IN_CLAUSE_CHUNK_SIZE = 1000;

    private final Environment environment;

    private boolean isLoggingEnabled;
    private boolean isSqlTracingEnabled;
    private boolean isSqlQueriesTracingEnabled;
    private boolean isQueryCacheEnabled;
    private int inClauseChunkSize;
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
        inClauseChunkSize = getInt(IN_CLAUSE_CHUNK_SIZE.value(), DEFAULT_IN_CLAUSE_CHUNK_SIZE);
    }

    public int getInClauseChunkSize() {
        return inClauseChunkSize;
    }

    public boolean isQueryCacheEnabled() {
        return isQueryCacheEnabled;
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

    private boolean getBoolean(String propertyName) {
        String booleanString = environment.getProperty(propertyName);
        return Boolean.parseBoolean(booleanString == null ? "false" : booleanString);
    }

    @SuppressWarnings("unchecked")
    private List<String> getList(String propertyName) {
        return environment.getProperty(propertyName, List.class);
    }

    private int getInt(String propertyName, int fallback) {
        String value = environment.getProperty(propertyName);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Integer.parseInt(value.trim());
    }

    private String getString(String propertyName) {
        return environment.getProperty(propertyName);
    }
}
