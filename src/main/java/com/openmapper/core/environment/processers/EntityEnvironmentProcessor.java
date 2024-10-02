package com.openmapper.core.environment.processers;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.openmapper.annotations.entity.Model;
import com.openmapper.config.OpenMapperGlobalEnvironmentVariables;
import com.openmapper.core.PackageScanner;
import com.openmapper.core.common.ModelSpecificationReader;
import com.openmapper.core.context.EntityMetadataContext;
import com.openmapper.core.context.model.ModelMetadata;
import com.openmapper.core.environment.EnvironmentProcessor;

@Component
public class EntityEnvironmentProcessor implements EnvironmentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(EntityEnvironmentProcessor.class);

    private final PackageScanner scanner;
    private final OpenMapperGlobalEnvironmentVariables variables;
    private final ModelSpecificationReader modelSpecificationReader;
    private final EntityMetadataContext entityMetadataContext;

    public EntityEnvironmentProcessor(
            PackageScanner scanner,
            OpenMapperGlobalEnvironmentVariables variables,
            ModelSpecificationReader modelSpecificationReader,
            EntityMetadataContext entityMetadataContext) {
        this.scanner = scanner;
        this.variables = variables;
        this.modelSpecificationReader = modelSpecificationReader;
        this.entityMetadataContext = entityMetadataContext;
    }

    @Override
    public void processEnvironment() {
        Set<Class<?>> classes = scanner.scanPackagesFor(variables.getDaoPackageToScan(), Model.class);

        for (Class<?> entityClass : classes) {
            ModelMetadata modelMetadata = modelSpecificationReader.readModelMetadata(entityClass);
            entityMetadataContext.registerEntity(entityClass, modelMetadata);
        }
    }
}
