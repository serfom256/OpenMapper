package io.github.serfom256.core.query.representation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EntityGraph {

    private final Map<Class<?>, Map<Object, Object>> byType = new HashMap<>();

    public Map<Object, Object> forType(Class<?> type) {
        return byType.computeIfAbsent(type, t -> new LinkedHashMap<>());
    }

    public boolean isEmpty() {
        return byType.isEmpty();
    }
}
