package com.openmapper.common.reflect;

import java.util.Map;
import java.util.function.UnaryOperator;

import org.springframework.stereotype.Component;

@Component
public class FieldValueGenerator {

    private static final int DEFAULT_VALUE = 0;

    private static final Map<Class<?>, UnaryOperator<Object>> supportedValueActions = Map.ofEntries(
            Map.entry(short.class, v -> (short) ((short) v + 1)),
            Map.entry(Short.class, v -> v == null ? DEFAULT_VALUE : (short) ((short) v + 1)),
            Map.entry(int.class, v -> (int) ((int) v + 1)),
            Map.entry(Integer.class, v -> v == null ? DEFAULT_VALUE : (int) ((int) v + 1)),
            Map.entry(long.class, v -> (long) ((long) v + 1)),
            Map.entry(Long.class, v -> v == null ? DEFAULT_VALUE : (long) ((long) v + 1)));

    public Object getNextOptimisticLockFieldValue(Object previousValue) {
        if (previousValue == null) {
            return 0; // TODO get minimum value from bean
        }

        return supportedValueActions
                .get(previousValue.getClass())
                .apply(previousValue);
    }
}
