package com.openmapper.mappers;

import com.openmapper.common.reflect.FieldValueGenerator;
import com.openmapper.common.reflect.ObjectUtils;
import com.openmapper.core.query.model.ModelSpecifications;
import com.openmapper.core.query.model.QuerySpecifications;
import com.openmapper.annotations.entity.Field;
import com.openmapper.annotations.entity.Nested;
import com.openmapper.annotations.entity.OptimisticLockField;

import static com.openmapper.common.operations.DmlOperation.UPDATE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ModelExtractor {

    private final FieldValueGenerator fieldValueGenerator = new FieldValueGenerator();

    public ModelSpecifications extractSpecification(Object entity, QuerySpecifications specifications) {
        if (entity == null) {
            return new ModelSpecifications(Collections.emptyMap());
        }

        Map<String, Object> values = new HashMap<>();
        ModelSpecifications modelSpecifications = new ModelSpecifications(values);
        Object previousOptimisticLockValue = specifications.getPreviousOptimisticLockValue();

        for (java.lang.reflect.Field field : entity.getClass().getDeclaredFields()) {
            final Field annotation = field.getAnnotation(Field.class);

            if (annotation != null) {
                values.put(annotation.name().isEmpty() ? field.getName() : annotation.name(),
                        ObjectUtils.getFieldValue(entity, field));

            } else if (field.getAnnotation(Nested.class) != null) {
                ModelSpecifications nestedSpec = extractSpecification(ObjectUtils.getFieldValue(entity, field), specifications);
                values.putAll(nestedSpec.getParams());
                if (specifications.getOperation() == UPDATE) {
                    mapToModelSpecifications(modelSpecifications,
                            nestedSpec.getOptimisticLockName(),
                            nestedSpec.getGeneratedOptimisticLockValue(),
                            previousOptimisticLockValue);
                }

            } else if (field.getAnnotation(OptimisticLockField.class) != null) {
                OptimisticLockField lockField = field.getAnnotation(OptimisticLockField.class);

                if (specifications.getOperation() == UPDATE) {
                    Object prevValue = previousOptimisticLockValue;
                    if (prevValue == null) {
                        prevValue = ObjectUtils.getFieldValue(entity, field);
                    }

                    Object nextValue = fieldValueGenerator.getNextOptimisticLockFieldValue(prevValue);

                    mapToModelSpecifications(modelSpecifications, field.getName(), nextValue, prevValue);
                    values.put(lockField.name().isEmpty() ? field.getName() : lockField.name(), nextValue);
                    values.put(field.getAnnotation(OptimisticLockField.class).oldVersion(), prevValue);
                } else {
                    values.put(lockField.name().isEmpty() ? field.getName() : lockField.name(),
                            ObjectUtils.getFieldValue(entity, field));
                }

            }
        }
        return modelSpecifications;
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