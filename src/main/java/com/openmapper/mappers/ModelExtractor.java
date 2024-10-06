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

    public ModelSpecifications extractSpecification(Object entity, QuerySpecifications querySpecifications) {
        if (entity == null) {
            return new ModelSpecifications(Collections.emptyMap());
        }
        
        Map<String, Object> values = new HashMap<>();
        ModelSpecifications modelSpecifications = new ModelSpecifications(values);
        Object previousOptimisticLockValue = querySpecifications.getPreviousOptimisticLockValue();

        for (java.lang.reflect.Field field : entity.getClass().getDeclaredFields()) {
            final Field annotation = field.getAnnotation(Field.class);
            if (annotation != null) {
                values.put(annotation.name().isEmpty() ? field.getName() : annotation.name(),
                        ObjectUtils.getFieldValue(entity, field));
            } else if (field.getAnnotation(Nested.class) != null) {
                ModelSpecifications nestedSpec = extractSpecification(ObjectUtils.getFieldValue(entity, field), querySpecifications);
                values.putAll(nestedSpec.getParams());
                if (querySpecifications.getOperation() == UPDATE) { // TODO just replace previous optimistic lock by name in map
                    modelSpecifications.setOptimisticLockName(nestedSpec.getOptimisticLockName());
                    modelSpecifications.setGeneratedOptimisticLockValue(nestedSpec.getGeneratedOptimisticLockValue());
                    modelSpecifications.setPreviousOptimisticLockValue(previousOptimisticLockValue);
                }

            } else if (field.getAnnotation(OptimisticLockField.class) != null) {

                OptimisticLockField optimisticLockField = field.getAnnotation(OptimisticLockField.class);
                Object prevValue = previousOptimisticLockValue;
                if (querySpecifications.getOperation() == UPDATE && prevValue == null) {
                    prevValue = ObjectUtils.getFieldValue(entity, field);
                }

                Object nextOptimisticLockValue = fieldValueGenerator.getNextOptimisticLockFieldValue(prevValue);
                modelSpecifications.setOptimisticLockName(field.getName());
                modelSpecifications.setGeneratedOptimisticLockValue(nextOptimisticLockValue);
                modelSpecifications.setPreviousOptimisticLockValue(prevValue);

                values.put(optimisticLockField.name().isEmpty() ? field.getName() : optimisticLockField.name(), nextOptimisticLockValue);

                values.put(field.getAnnotation(OptimisticLockField.class).oldVersion(), prevValue);
            }
        }
        return modelSpecifications;
    }
}
