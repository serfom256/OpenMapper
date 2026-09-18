package io.github.serfom256.core.environment.processors;

import java.lang.reflect.Field;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.serfom256.annotations.entity.Dto;
import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.annotations.entity.Nested;
import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import io.github.serfom256.core.common.ModelSpecificationReader;
import io.github.serfom256.core.context.ModelMetadataContext;
import io.github.serfom256.core.context.model.ModelMetadata;
import io.github.serfom256.core.environment.EnvironmentProcessor;
import io.github.serfom256.core.environment.PackageScanner;
import io.github.serfom256.core.query.mapping.MappingPlanRegistry;

public class ModelEnvironmentProcessor implements EnvironmentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ModelEnvironmentProcessor.class);

    private final PackageScanner scanner;
    private final OpenMapperGlobalEnvironmentVariables variables;
    private final ModelSpecificationReader modelSpecificationReader;
    private final ModelMetadataContext modelMetadataContext;
    private final MappingPlanRegistry mappingPlanRegistry;

    public ModelEnvironmentProcessor(
            PackageScanner scanner,
            OpenMapperGlobalEnvironmentVariables variables,
            ModelSpecificationReader modelSpecificationReader,
            ModelMetadataContext entityMetadataContext,
            MappingPlanRegistry mappingPlanRegistry) {
        this.scanner = scanner;
        this.variables = variables;
        this.modelSpecificationReader = modelSpecificationReader;
        this.modelMetadataContext = entityMetadataContext;
        this.mappingPlanRegistry = mappingPlanRegistry;
    }

    @Override
    public void processEnvironment() {
        String packageToScan = modelPackage();
        Set<Class<?>> modelClasses = scanner.scanPackagesFor(packageToScan, Model.class);
        Set<Class<?>> dtoClasses = scanner.scanPackagesFor(packageToScan, Dto.class);

        modelClasses.forEach(this::addModelToContext);
        dtoClasses.forEach(this::addModelToContext);

        modelClasses.forEach(mappingPlanRegistry::planFor);
    }

    private String modelPackage() {
        String modelPackage = variables.getModelPackageToScan();
        return modelPackage != null ? modelPackage : variables.getDaoPackageToScan();
    }

    private void addModelToContext(Class<?> modelClass) {
        if (variables.isLoggingEnabled()) {
            logger.info("Found model of type: {}", modelClass.getName());
        }
        if (modelMetadataContext.isRegistered(modelClass)) {
            return;
        }
        ModelMetadata modelMetadata = modelSpecificationReader.readModelMetadata(modelClass);
        modelMetadataContext.registerModel(modelClass, modelMetadata);
        processModelNestedFields(modelClass);
    }

    private void processModelNestedFields(Class<?> modelClass) {
        Field[] declaredFields = io.github.serfom256.common.reflect.ObjectUtils.mappedFields(modelClass);
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Nested.class)) {
                addModelToContext(field.getType());
            }
        }
    }
}
