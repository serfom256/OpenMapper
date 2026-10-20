package io.github.serfom256.core.query.representation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

final class ObjectKeyedIndex implements InstanceIndex {

    private final Map<Object, Object> instances = new LinkedHashMap<>();

    @Override
    public Object get(long key) {
        return instances.get(key);
    }

    @Override
    public Object get(Object key) {
        return instances.get(key);
    }

    @Override
    public void put(long key, Object instance) {
        instances.put(key, instance);
    }

    @Override
    public void put(Object key, Object instance) {
        instances.put(key, instance);
    }

    @Override
    public Collection<Object> values() {
        return instances.values();
    }
}
