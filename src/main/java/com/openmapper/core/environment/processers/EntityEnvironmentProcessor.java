package com.openmapper.core.environment.processers;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmapper.annotations.entity.Model;
import com.openmapper.config.OpenMapperGlobalEnvironmentVariables;
import com.openmapper.core.PackageScanner;
import com.openmapper.core.environment.EnvironmentProcessor;

public class EntityEnvironmentProcessor implements EnvironmentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(EntityEnvironmentProcessor.class);

    private final PackageScanner scanner;
    private final OpenMapperGlobalEnvironmentVariables variables;

    public EntityEnvironmentProcessor(PackageScanner scanner, OpenMapperGlobalEnvironmentVariables variables) {
        this.scanner = scanner;
        this.variables = variables;
    }

    @Override
    public void processEnvironment() {
        Set<Class<?>> classes = scanner.scanPackagesFor(variables.getDaoPackageToScan(), Model.class);

        for (Class<?> entityClass : classes) {

        }
    }

}
