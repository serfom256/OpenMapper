package io.github.serfom256.core.lifecycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.serfom256.model.operations.DmlOperation;

public class EntityWriteListeners {

    private final List<EntityWriteListener<?>> listeners;
    private final Map<Class<?>, List<EntityWriteListener<?>>> byType = new ConcurrentHashMap<>();

    public EntityWriteListeners() {
        this(List.of());
    }

    public EntityWriteListeners(Collection<EntityWriteListener<?>> listeners) {
        this.listeners = List.copyOf(listeners);
    }

    public boolean isEmpty() {
        return listeners.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public void beforeWrite(Object entity, DmlOperation operation) {
        if (listeners.isEmpty() || entity == null || operation == DmlOperation.SELECT) {
            return;
        }
        for (EntityWriteListener<?> listener : applicableTo(entity.getClass())) {
            ((EntityWriteListener<Object>) listener).beforeWrite(entity, operation);
        }
    }

    private List<EntityWriteListener<?>> applicableTo(Class<?> entityType) {
        return byType.computeIfAbsent(entityType, type -> {
            List<EntityWriteListener<?>> applicable = new ArrayList<>();
            for (EntityWriteListener<?> listener : listeners) {
                if (listener.type().isAssignableFrom(type)) {
                    applicable.add(listener);
                }
            }
            return List.copyOf(applicable);
        });
    }
}
