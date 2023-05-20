package com.openmapper.mappers;

import com.openmapper.common.ObjectUtils;
import com.openmapper.core.annotations.entity.Field;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


public class EntityPropertyExtractor {

    private EntityPropertyExtractor() {
    }

    public static Map<String, Object> extractParams(Object entity) {
        final Map<String, Object> values = new HashMap<>();
        for (java.lang.reflect.Field field : entity.getClass().getDeclaredFields()) {
            Field annotation = field.getAnnotation(Field.class);
            if (annotation != null) {
                values.put(annotation.name().isEmpty() ? field.getName() : annotation.name(), ObjectUtils.getFieldValue(entity, field));
            }
        }
        return values;
    }
}
