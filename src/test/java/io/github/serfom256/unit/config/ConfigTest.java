package io.github.serfom256.unit.config;

import io.github.serfom256.config.OpenMapperGlobalConstants;
import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigTest {

    @Nested
    class GlobalConstantsTest {
        @Test
        void fileExtension() {
            assertEquals(".fsql", OpenMapperGlobalConstants.FILE_EXTENSION.value());
        }

        @Test
        void allConstantsHaveValues() {
            for (OpenMapperGlobalConstants c : OpenMapperGlobalConstants.values()) {
                assertNotNull(c.value());
                assertFalse(c.value().isBlank());
            }
        }

        @Test
        void valueOf_roundTrips() {
            assertEquals(OpenMapperGlobalConstants.FILE_EXTENSION,
                    OpenMapperGlobalConstants.valueOf("FILE_EXTENSION"));
        }

        @Test
        void propertyNames() {
            assertEquals("openmapper.fsql.path", OpenMapperGlobalConstants.FSQL_FILES_PATH.value());
            assertEquals("openmapper.dao.packagesToScan", OpenMapperGlobalConstants.DAO_PACKAGE_TO_SCAN.value());
            assertEquals("openmapper.logging.enabled", OpenMapperGlobalConstants.LOGGING_ENABLED.value());
            assertEquals("openmapper.query.cache.enabled", OpenMapperGlobalConstants.QUERY_CACHE_ENABLED.value());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    class GlobalEnvironmentVariablesTest {

        @Mock Environment environment;

        @Test
        void defaultValues_allFalse() {
            OpenMapperGlobalEnvironmentVariables vars = new OpenMapperGlobalEnvironmentVariables(environment);
            assertFalse(vars.isLoggingEnabled());
            assertFalse(vars.isSqlTracingEnabled());
            assertFalse(vars.isSqlQueryTracingEnabled());
            assertFalse(vars.isQueryCacheEnabled());
        }

        @Test
        void loggingEnabled_whenPropertySet() {
            when(environment.getProperty("openmapper.logging.enabled")).thenReturn("true");
            OpenMapperGlobalEnvironmentVariables vars = new OpenMapperGlobalEnvironmentVariables(environment);
            assertTrue(vars.isLoggingEnabled());
        }

        @Test
        void sqlTracingEnabled() {
            when(environment.getProperty("openmapper.sql.tracing.enabled")).thenReturn("true");
            OpenMapperGlobalEnvironmentVariables vars = new OpenMapperGlobalEnvironmentVariables(environment);
            assertTrue(vars.isSqlTracingEnabled());
        }

        @Test
        void packageToScan() {
            when(environment.getProperty("openmapper.dao.packagesToScan")).thenReturn("com.example.dao");
            OpenMapperGlobalEnvironmentVariables vars = new OpenMapperGlobalEnvironmentVariables(environment);
            assertEquals("com.example.dao", vars.getDaoPackageToScan());
        }

        @Test
        void sqlFilePaths_null() {
            OpenMapperGlobalEnvironmentVariables vars = new OpenMapperGlobalEnvironmentVariables(environment);
            assertNull(vars.getSqlFilePaths());
        }

        @Test
        void queryCacheEnabled() {
            when(environment.getProperty("openmapper.query.cache.enabled")).thenReturn("true");
            OpenMapperGlobalEnvironmentVariables vars = new OpenMapperGlobalEnvironmentVariables(environment);
            assertTrue(vars.isQueryCacheEnabled());
        }

        @Test
        void modelPackageToScan() {
            when(environment.getProperty("openmapper.model.packagesToScan")).thenReturn("com.example.model");
            OpenMapperGlobalEnvironmentVariables vars = new OpenMapperGlobalEnvironmentVariables(environment);
            assertEquals("com.example.model", vars.getModelPackageToScan());
        }
    }
}
