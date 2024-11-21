package com.openmapper.core.context;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.openmapper.core.context.model.ModelMetadata;

@Component
public class ModelMetadataContext {

    private final ConcurrentHashMap<Class<?>, ModelMetadata> concurrentHashMap = new ConcurrentHashMap<>();
    private final ModelMetadata emptyModelMetadata = new ModelMetadata(null, null);

    public void registerModel(Class<?> modelClass, ModelMetadata modelMetadata) {
        concurrentHashMap.put(modelClass, modelMetadata);
    }

    public ModelMetadata getMetadataByType(Class<?> modelClass) {
        return concurrentHashMap.getOrDefault(modelClass, emptyModelMetadata);
    }
}
