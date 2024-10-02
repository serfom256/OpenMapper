package com.openmapper.core.common;

import java.lang.reflect.Field;

import org.springframework.stereotype.Component;

import com.openmapper.annotations.entity.OptimisticLockField;
import com.openmapper.core.context.model.ModelMetadata;
import com.openmapper.exceptions.entity.ModelValidationException;

@Component
public class ModelSpecificationReader {

    public ModelMetadata readModelMetadata(Class<?> modelClass) {

        Field[] declaredFields = modelClass.getDeclaredFields();

        Field optimisticLockField = findOptimisticLockField(declaredFields, modelClass);

        return new ModelMetadata(optimisticLockField);
    }

    private Field findOptimisticLockField(Field[] declaredFields, Class<?> modelClass) {
        int optimisticLockFieldCount = 0;
        Field optimisticLockField = null;

        for (Field field : declaredFields) {
            if (field.getDeclaredAnnotation(OptimisticLockField.class) != null) {
                optimisticLockField = field; // TODO add field type validation
            }
        }
        if (optimisticLockFieldCount > 1) {
            throw new ModelValidationException(modelClass,
                    "invalid @OptimisticLockField field count: expected 1, got %s"
                            .formatted(optimisticLockField));
        }

        return optimisticLockField;
    }

}
