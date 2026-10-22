package io.github.serfom256.core.environment;

import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

public class PackageScanner {

    private static final String NO_ROOT_PACKAGE =
            "openMapper found no package to scan for @{}: there is no @SpringBootApplication bean and"
                    + " neither openmapper.dao.packagesToScan nor openmapper.model.packagesToScan is set.";

    private final OpenMapperGlobalEnvironmentVariables variables;

    private final @Nullable ApplicationContext context;

    private final Environment environment;

    private final ClassLoader classLoader;

    private static final Logger logger = LoggerFactory.getLogger(PackageScanner.class);

    public PackageScanner(OpenMapperGlobalEnvironmentVariables variables, ApplicationContext context) {
        this(variables, context, context.getEnvironment(), context.getClassLoader());
    }

    public PackageScanner(OpenMapperGlobalEnvironmentVariables variables,
                          Environment environment,
                          ClassLoader classLoader) {
        this(variables, null, environment, classLoader);
    }

    private PackageScanner(OpenMapperGlobalEnvironmentVariables variables,
                           @Nullable ApplicationContext context,
                           Environment environment,
                           ClassLoader classLoader) {
        this.variables = variables;
        this.context = context;
        this.environment = environment;
        this.classLoader = classLoader;
    }

    public Set<Class<?>> scanPackagesFor(String packageToScan, Class<? extends Annotation> annotation) {
        if (packageToScan == null) {
            packageToScan = getRootPackage();
        }
        if (packageToScan == null) {
            logger.warn(NO_ROOT_PACKAGE, annotation.getSimpleName());
            return Set.of();
        }
        if (variables.isLoggingEnabled()) {
            logger.info("Scanning packages for @{} annotation: {}", annotation.getName(), packageToScan);
        }
        return new AnnotatedTypeScanner(environment, classLoader).scan(packageToScan, annotation);
    }

    private @Nullable String getRootPackage() {
        if (context == null) {
            return null;
        }
        Map<String, Object> annotatedBeans = context.getBeansWithAnnotation(SpringBootApplication.class);
        if (annotatedBeans.isEmpty()) {
            return null;
        }
        return annotatedBeans.values().iterator().next().getClass().getPackage().getName();
    }
}
