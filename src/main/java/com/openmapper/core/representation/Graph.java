package com.openmapper.core.representation;

import java.util.HashMap;
import java.util.Map;

public class Graph {

    private final Map<Class<?>, Map<Object, Object>> entityGraph;

    public Graph() {
        entityGraph = new HashMap<>(4);
    }

    public Map<Object, Object> getIfAbsent(final Class<?> clazz) {
        return entityGraph.computeIfAbsent(clazz, m -> new HashMap<>());
    }

    public Map<Object, Object> get(final Class<?> clazz) {
        return entityGraph.get(clazz);
    }

}
