package io.github.serfom256.core.environment;

import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

public class PackageScanner {

    private static final String NO_ROOT_PACKAGE =
            "openMapper found no package to scan for @{}: there is no @SpringBootApplication bean and"
                    + " neither openmapper.dao.packagesToScan nor openmapper.model.packagesToScan is set.";

    private final OpenMapperGlobalEnvironmentVariables variables;

    private final ApplicationContext context;

    private static final Logger logger = LoggerFactory.getLogger(PackageScanner.class);

    public PackageScanner(OpenMapperGlobalEnvironmentVariables variables, ApplicationContext context) {
        this.variables = variables;
        this.context = context;
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
        Reflections reflections = new Reflections(packageToScan);
        return reflections.getTypesAnnotatedWith(annotation);
    }

    private @Nullable String getRootPackage() {
        Map<String, Object> annotatedBeans = context.getBeansWithAnnotation(SpringBootApplication.class);
        if (annotatedBeans.isEmpty()) {
            return null;
        }
        return annotatedBeans.values().iterator().next().getClass().getPackage().getName();
    }
}
