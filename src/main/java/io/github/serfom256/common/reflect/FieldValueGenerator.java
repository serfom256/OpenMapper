package io.github.serfom256.common.reflect;

import java.util.Map;
import java.util.function.UnaryOperator;

public class FieldValueGenerator {

    private static final int DEFAULT_VALUE = 0;

    private static final Map<Class<?>, UnaryOperator<Object>> supportedValueActions = Map.ofEntries(
            Map.entry(short.class, v -> (short) ((short) v + 1)),
            Map.entry(Short.class, v -> v == null ? DEFAULT_VALUE : (short) ((short) v + 1)),
            Map.entry(int.class, v -> (int) ((int) v + 1)),
            Map.entry(Integer.class, v -> v == null ? DEFAULT_VALUE : (int) ((int) v + 1)),
            Map.entry(long.class, v -> (long) ((long) v + 1)),
            Map.entry(Long.class, v -> v == null ? DEFAULT_VALUE : (long) ((long) v + 1)));

    /**
     * Whether a field of this type can carry an optimistic-lock version.
     *
     * <p>Checked when the model is validated at startup, because an unsupported
     * type would otherwise fail on the first update that needs to increment it.
     */
    public static boolean supportsOptimisticLockType(Class<?> type) {
        return supportedValueActions.containsKey(type);
    }

    public static java.util.Set<Class<?>> supportedOptimisticLockTypes() {
        return supportedValueActions.keySet();
    }

    public Object getNextOptimisticLockFieldValue(Object previousValue) {
        if (previousValue == null) {
            return 0;
        }

        return supportedValueActions
                .get(previousValue.getClass())
                .apply(previousValue);
    }
}
