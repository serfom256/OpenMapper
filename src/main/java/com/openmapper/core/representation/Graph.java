package com.openmapper.core.representation;

import java.util.HashMap;
import java.util.Map;

/**
 * Graph represents mapped class type <-> primary-key <-> entity instance association
 */
public class Graph {

    private final Map<Class<?>, Map<Object, Object>> entityGraph;

    public Graph() {
        this.entityGraph = new HashMap<>(4);
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

}
