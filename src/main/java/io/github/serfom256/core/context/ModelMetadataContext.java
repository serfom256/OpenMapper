package io.github.serfom256.core.context;

import java.util.concurrent.ConcurrentHashMap;

import io.github.serfom256.core.context.model.ModelMetadata;
import io.github.serfom256.exceptions.entity.ModelValidationException;

public class ModelMetadataContext {

    private final ConcurrentHashMap<Class<?>, ModelMetadata> concurrentHashMap = new ConcurrentHashMap<>();

    public void registerModel(Class<?> modelClass, ModelMetadata modelMetadata) {
        concurrentHashMap.put(modelClass, modelMetadata);
    }

    public ModelMetadata getMetadataByType(Class<?> modelClass) {
        ModelMetadata metadata = concurrentHashMap.get(modelClass);
        if (metadata == null) {
            throw new ModelValidationException(modelClass,
                    "type is not a registered @Model/@Dto — ensure it is annotated and within openmapper.dao.packagesToScan");
        }
        return metadata;
    }

    public boolean isRegistered(Class<?> modeClass) {
        return concurrentHashMap.containsKey(modeClass);
    }
}
