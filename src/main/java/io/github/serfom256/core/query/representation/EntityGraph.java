package io.github.serfom256.core.query.representation;

import java.util.HashMap;
import java.util.Map;

public final class EntityGraph {

    private final Map<Class<?>, InstanceIndex> byType = new HashMap<>();

    public InstanceIndex forType(Class<?> type, boolean numericKey) {
        return byType.computeIfAbsent(type, t -> numericKey ? new LongKeyedIndex() : new ObjectKeyedIndex());
    }
}
