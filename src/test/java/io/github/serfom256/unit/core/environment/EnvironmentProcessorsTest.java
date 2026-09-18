package io.github.serfom256.unit.core.environment;

import io.github.serfom256.core.environment.EnvironmentProcessor;
import io.github.serfom256.core.environment.OpenMapperEnvironmentProcessor;
import io.github.serfom256.core.environment.PackageScanner;
import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.lang.annotation.*;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnvironmentProcessorsTest {

    @Nested
    class OpenMapperEnvironmentProcessorTest {

        @Test
        void constructor_callsProcessEnvironmentOnAll() {
            EnvironmentProcessor p1 = mock(EnvironmentProcessor.class);
            EnvironmentProcessor p2 = mock(EnvironmentProcessor.class);

            new OpenMapperEnvironmentProcessor(List.of(p1, p2));

            verify(p1).processEnvironment();
            verify(p2).processEnvironment();
        }

        @Test
        void constructor_emptyList_noOp() {
            assertDoesNotThrow(() -> new OpenMapperEnvironmentProcessor(List.of()));
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class PackageScannerTest {

        @Mock OpenMapperGlobalEnvironmentVariables variables;
        @Mock ApplicationContext context;

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.TYPE)
        @interface TestAnnotation {}

        @TestAnnotation
        static class AnnotatedClass {}

        @Test
        void scanPackages_findsAnnotatedClasses() {
            PackageScanner scanner = new PackageScanner(variables, context);
            Set<Class<?>> result = scanner.scanPackagesFor(
                    "io.github.serfom256.unit.core.environment", TestAnnotation.class);
            assertTrue(result.contains(AnnotatedClass.class));
        }

        @Test
        void scanPackages_emptyPackage_returnsEmpty() {
            PackageScanner scanner = new PackageScanner(variables, context);
            Set<Class<?>> result = scanner.scanPackagesFor(
                    "io.github.serfom256.nonexistent.package.xyz", TestAnnotation.class);
            assertTrue(result.isEmpty());
        }

        @Test
        void scanPackages_withLogging() {
            when(variables.isLoggingEnabled()).thenReturn(true);
            PackageScanner scanner = new PackageScanner(variables, context);
            assertDoesNotThrow(() -> scanner.scanPackagesFor(
                    "io.github.serfom256.unit.core.environment", TestAnnotation.class));
        }
    }
}
