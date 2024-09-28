package com.openmapper.core;

import com.openmapper.config.OpenMapperGlobalEnvironmentVariables;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

@Component
public class PackageScanner {

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
        if (variables.isLogging()) {
            logger.info("Scanning packages for @{} annotation: {}", annotation.getName(), packageToScan);
        }
        Reflections reflections = new Reflections(packageToScan);
        return reflections.getTypesAnnotatedWith(annotation);
    }

    private String getRootPackage() {
        Map<String, Object> annotatedBeans = context.getBeansWithAnnotation(SpringBootApplication.class);
        return annotatedBeans.values().toArray()[0].getClass().getPackage().getName();
    }
}
