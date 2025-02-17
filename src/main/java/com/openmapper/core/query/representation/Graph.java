package com.openmapper.core.query.representation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Graph represents mapped class type <-> primary-key <-> entity instance
 * association
 */
public class Graph {

    private final Map<Class<?>, Map<Object, Object>> entityGraph = new HashMap<>(4);
    private final Set<String> availableColumns;

    public Graph(Set<String> availableColumns) {
        this.availableColumns = availableColumns;
    }

    public Map<Object, Object> getIfAbsent(final Class<?> clazz) {
        return entityGraph.computeIfAbsent(clazz, m -> new HashMap<>());
    }

    public Map<Object, Object> get(final Class<?> clazz) {
        return entityGraph.get(clazz);
    }

    public boolean isEmpty() {
        return entityGraph.isEmpty();
    }

    public boolean containsColumn(String name) {
        return availableColumns.contains(name);
    }
}
