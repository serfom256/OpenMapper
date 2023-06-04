package com.openmapper.common;

import com.openmapper.config.OpenMapperGlobalContext;
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

    private final OpenMapperGlobalContext globalContext;

    private final ApplicationContext context;

    private static final Logger logger = LoggerFactory.getLogger(PackageScanner.class);

    public PackageScanner(OpenMapperGlobalContext globalContext, ApplicationContext context) {
        this.globalContext = globalContext;
        this.context = context;
    }

    public Set<Class<?>> scanPackagesFor(String packageToScan, Class<? extends Annotation> annotation) {
        if (packageToScan == null) packageToScan = getRootPackage();
        if (globalContext.isLogging()) {
            logger.info("Scanning packages for @DaoLayer annotation: {}", packageToScan);
        }
        Reflections reflections = new Reflections(packageToScan);
        return reflections.getTypesAnnotatedWith(annotation);
    }

    private String getRootPackage() {
        Map<String, Object> annotatedBeans = context.getBeansWithAnnotation(SpringBootApplication.class);
        return annotatedBeans.values().toArray()[0].getClass().getPackage().getName();
    }
}
