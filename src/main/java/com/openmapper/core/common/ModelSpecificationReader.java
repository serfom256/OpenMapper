package com.openmapper.core.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.openmapper.annotations.entity.Dto;
import com.openmapper.annotations.entity.Joined;
import com.openmapper.annotations.entity.Nested;
import com.openmapper.annotations.entity.OptimisticLockField;
import com.openmapper.core.context.model.ModelMetadata;
import com.openmapper.exceptions.entity.ModelValidationException;

@Component
public class ModelSpecificationReader {

    private final Set<Class<? extends Annotation>> mappableFields = Set.of(
            com.openmapper.annotations.entity.Field.class,
            Dto.class,
            Joined.class,
            Nested.class,
            OptimisticLockField.class);

    public ModelMetadata readModelMetadata(Class<?> modelClass) {

        Field[] declaredFields = modelClass.getDeclaredFields();

        Map<Field, Annotation> mappedFields = findMappedFields(declaredFields);
        Field optimisticLockField = findOptimisticLockField(declaredFields, modelClass);

        return new ModelMetadata(optimisticLockField, mappedFields);
    }

    private Field findOptimisticLockField(Field[] declaredFields, Class<?> modelClass) {
        int optimisticLockFieldCount = 0;
        Field optimisticLockField = null;

        if (modelClass.getAnnotation(Dto.class) != null) {
            return optimisticLockField;
        }

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

    private Map<Field, Annotation> findMappedFields(Field[] declaredFields) {
        Map<Field, Annotation> modelDeclaredFieldAnnotations = new HashMap<>();
        for (Field field : declaredFields) {
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (mappableFields.contains(annotation.annotationType())) {
                    modelDeclaredFieldAnnotations.put(field, annotation);
                }
            }
        }

        return modelDeclaredFieldAnnotations;
    }
}
