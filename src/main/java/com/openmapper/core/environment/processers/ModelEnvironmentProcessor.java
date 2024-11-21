package com.openmapper.core.environment.processers;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.openmapper.annotations.entity.Dto;
import com.openmapper.annotations.entity.Model;
import com.openmapper.config.OpenMapperGlobalEnvironmentVariables;
import com.openmapper.core.common.ModelSpecificationReader;
import com.openmapper.core.context.ModelMetadataContext;
import com.openmapper.core.context.model.ModelMetadata;
import com.openmapper.core.environment.EnvironmentProcessor;
import com.openmapper.core.environment.PackageScanner;

@Component
public class ModelEnvironmentProcessor implements EnvironmentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ModelEnvironmentProcessor.class);

    private final PackageScanner scanner;
    private final OpenMapperGlobalEnvironmentVariables variables;
    private final ModelSpecificationReader modelSpecificationReader;
    private final ModelMetadataContext modelMetadataContext;

    public ModelEnvironmentProcessor(
            PackageScanner scanner,
            OpenMapperGlobalEnvironmentVariables variables,
            ModelSpecificationReader modelSpecificationReader,
            ModelMetadataContext entityMetadataContext) {
        this.scanner = scanner;
        this.variables = variables;
        this.modelSpecificationReader = modelSpecificationReader;
        this.modelMetadataContext = entityMetadataContext;
    }

    @Override
    public void processEnvironment() {
        Set<Class<?>> modelClasses = scanner.scanPackagesFor(variables.getDaoPackageToScan(), Model.class);
        Set<Class<?>> dtoClasses = scanner.scanPackagesFor(variables.getDaoPackageToScan(), Dto.class);

        modelClasses.forEach(this::addModelToContext);
        dtoClasses.forEach(this::addModelToContext);
    }

    private void addModelToContext(Class<?> modelClass) {
        if (variables.isLoggingEnabled()) {
            logger.info("Found model of type: {}", modelClass.getName());
        }
        ModelMetadata modelMetadata = modelSpecificationReader.readModelMetadata(modelClass);
        modelMetadataContext.registerModel(modelClass, modelMetadata);
    }
}
