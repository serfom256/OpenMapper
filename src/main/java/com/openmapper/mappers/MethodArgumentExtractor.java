package com.openmapper.mappers;

import com.openmapper.common.reflect.FieldValueGenerator;
import com.openmapper.common.reflect.ObjectUtils;
import com.openmapper.annotations.entity.Field;
import com.openmapper.annotations.entity.Nested;
import com.openmapper.annotations.entity.Versioned;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class MethodArgumentExtractor {

    private final FieldValueGenerator fieldValueGenerator = new FieldValueGenerator();

    public Map<String, Object> extractMethodArguments(Object entity) {
        if (entity == null) {
            return Collections.emptyMap();
        }
        final Map<String, Object> values = new HashMap<>();

        for (java.lang.reflect.Field field : entity.getClass().getDeclaredFields()) {
            final Field annotation = field.getAnnotation(Field.class);
            if (annotation != null) {
                values.put(annotation.name().isEmpty() ? field.getName() : annotation.name(), ObjectUtils.getFieldValue(entity, field));
            } else if (field.getAnnotation(Nested.class) != null) {
                values.putAll(extractMethodArguments(ObjectUtils.getFieldValue(entity, field)));
            }else if (field.getAnnotation(Versioned.class) != null) {
                values.put(field.getAnnotation(Versioned.class).name(), fieldValueGenerator.getNextVersionedFieldValue(field, entity));
            }
        }
        return values;
    }
}
