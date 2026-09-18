package io.github.serfom256.core.query.representation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Identity map for a single result-set materialisation: for each entity type it
 * maps primary-key value to the entity instance, preserving first-seen order so
 * that result lists keep the order returned by the query.
 */
public final class EntityGraph {

    private final Map<Class<?>, Map<Object, Object>> byType = new HashMap<>();

    public Map<Object, Object> forType(Class<?> type) {
        return byType.computeIfAbsent(type, t -> new LinkedHashMap<>());
    }

    public boolean isEmpty() {
        return byType.isEmpty();
    }
}
