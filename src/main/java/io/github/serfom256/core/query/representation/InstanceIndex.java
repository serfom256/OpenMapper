package io.github.serfom256.core.query.representation;

import java.util.Collection;

public interface InstanceIndex {

    Object get(long key);

    Object get(Object key);

    void put(long key, Object instance);

    void put(Object key, Object instance);

    Collection<Object> values();
}
