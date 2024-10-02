package com.openmapper.common.reflect;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.springframework.stereotype.Component;

import com.openmapper.exceptions.common.UnsupportedTypeException;

@Component
public class FieldValueGenerator {

    private static final int DEFAULT_VALUE = 0;

    private static final Map<Class<?>, UnaryOperator<Object>> supportedValueActions = Map.ofEntries(
            Map.entry(short.class, v -> (short) ((short) v + 1)),
            Map.entry(Short.class, v -> v == null ? DEFAULT_VALUE : (short) ((short) v + 1)),
            Map.entry(int.class, v -> (int) ((int) v + 1)),
            Map.entry(Integer.class, v -> v == null ? DEFAULT_VALUE : (int) ((int) v + 1)),
            Map.entry(long.class, v -> (long) ((long) v + 1)),
            Map.entry(Long.class, v -> v == null ? DEFAULT_VALUE : (long) ((long) v + 1)),
            Map.entry(Timestamp.class, v -> new Timestamp(System.currentTimeMillis())));

    public Object getNextOptimisticLockFieldValue(Field field, Object object) {
        final Class<?> fieldType = field.getType();

        if (!supportedValueActions.containsKey(fieldType)) {
            throw new UnsupportedTypeException(fieldType.getTypeName());
        }
        final Object fieldValue = ObjectUtils.getFieldValue(object, field);
        supportedValueActions
                .get(fieldType)
                .apply(fieldValue);

        return fieldValue;
    }
}
