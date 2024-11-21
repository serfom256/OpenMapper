package com.openmapper.mappers;

import com.openmapper.common.reflect.FieldValueGenerator;
import com.openmapper.common.reflect.ObjectUtils;
import com.openmapper.core.context.ModelMetadataContext;
import com.openmapper.core.context.model.ModelMetadata;
import com.openmapper.core.query.model.ModelSpecifications;
import com.openmapper.core.query.model.QuerySpecifications;
import com.openmapper.annotations.entity.Nested;
import com.openmapper.annotations.entity.OptimisticLockField;

import static com.openmapper.model.operations.DmlOperation.UPDATE;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ModelExtractor {

    private final FieldValueGenerator fieldValueGenerator;
    private final ModelMetadataContext modelMetadataContext;

    public ModelExtractor(FieldValueGenerator fieldValueGenerator, ModelMetadataContext modelMetadataContext) {
        this.fieldValueGenerator = fieldValueGenerator;
        this.modelMetadataContext = modelMetadataContext;
    }

    public ModelSpecifications extractSpecification(Object entity, QuerySpecifications specifications) {
        if (entity == null) {
            return new ModelSpecifications(Collections.emptyMap());
        }

        final ModelMetadata modelMetadata = modelMetadataContext.getMetadataByType(entity.getClass());
        final Map<Field, Annotation> annotatedFields = modelMetadata.getAnnotatedFields();

        final Map<String, Object> values = new HashMap<>();
        final ModelSpecifications modelSpecifications = new ModelSpecifications(values);
        final Object prevLockValue = specifications.getPreviousOptimisticLockValue();

        for (Map.Entry<Field, Annotation> entry : annotatedFields.entrySet()) {
            final Class<? extends Annotation> annotationType = entry.getValue().annotationType();
            final Field f = entry.getKey();
            final Annotation annotation = entry.getValue();
            final Object fieldValue = ObjectUtils.getFieldValue(entity, f);

            if (annotationType.equals(com.openmapper.annotations.entity.Field.class)) {
                values.put(getFieldName(annotation, f), fieldValue);
                
            } else if (annotationType.equals(Nested.class)) {
                ModelSpecifications nestedSpec = extractSpecification(fieldValue, specifications);
                values.putAll(nestedSpec.getParams());
                if (specifications.getOperation() == UPDATE) {
                    mapToModelSpecifications(modelSpecifications, nestedSpec.getOptimisticLockName(),
                            nestedSpec.getGeneratedOptimisticLockValue(), prevLockValue);
                }

            } else if (annotationType.equals(OptimisticLockField.class)) {
                OptimisticLockField lockField = (OptimisticLockField) annotation;
                String name = getLockFieldName(lockField, f);

                if (specifications.getOperation() == UPDATE) {
                    Object prevValue = getLockFieldValue(prevLockValue, fieldValue);
                    Object nextValue = fieldValueGenerator.getNextOptimisticLockFieldValue(prevValue);

                    mapToModelSpecifications(modelSpecifications, f.getName(), nextValue, prevValue);
                    values.put(name, nextValue);
                    values.put(lockField.oldVersion(), prevValue);
                } else {
                    values.put(name, fieldValue);
                }
            }
        }

        return modelSpecifications;
    }

    private String getFieldName(Annotation annotation, Field field) {
        com.openmapper.annotations.entity.Field fieldAnnotation = (com.openmapper.annotations.entity.Field) annotation;
        return fieldAnnotation.name().isEmpty() ? field.getName() : fieldAnnotation.name();
    }

    private String getLockFieldName(OptimisticLockField lockField, Field field) {
        return lockField.name().isEmpty() ? field.getName() : lockField.name();
    }

    private Object getLockFieldValue(Object prevLockValue, Object fieldValue) {
        return prevLockValue == null ? fieldValue : prevLockValue;
    }

    private void mapToModelSpecifications(
            ModelSpecifications model,
            String optimisticLockName,
            Object lockValue,
            Object prevLockValue) {
        model.setOptimisticLockName(optimisticLockName);
        model.setGeneratedOptimisticLockValue(lockValue);
        model.setPreviousOptimisticLockValue(prevLockValue);
    }
}