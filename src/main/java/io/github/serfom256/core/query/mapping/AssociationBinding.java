package io.github.serfom256.core.query.mapping;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

/** Binds one {@code @Joined} association (to-one or to-many) of an entity. */
final class AssociationBinding {

    private final MethodHandle getter;
    private final MethodHandle setter;
    private final Class<?> targetType;
    private final boolean collection;
    private final boolean set;
    private final String joinByField;
    private final String toField;

    AssociationBinding(MethodHandle getter, MethodHandle setter, Class<?> targetType,
                       boolean collection, boolean set, String joinByField, String toField) {
        this.getter = getter;
        this.setter = setter;
        this.targetType = targetType;
        this.collection = collection;
        this.set = set;
        this.joinByField = joinByField;
        this.toField = toField;
    }

    Class<?> targetType() {
        return targetType;
    }

    String joinByField() {
        return joinByField;
    }

    String toField() {
        return toField;
    }

    boolean isCollection() {
        return collection;
    }

    void setSingle(Object owner, Object value) {
        ReflectiveAccessors.set(setter, owner, value);
    }

    /** Returns the owner's collection, creating and assigning an empty one if absent. */
    @SuppressWarnings("unchecked")
    Collection<Object> collectionOf(Object owner) {
        Object existing = ReflectiveAccessors.get(getter, owner);
        if (existing != null) {
            return (Collection<Object>) existing;
        }
        Collection<Object> created = set ? new LinkedHashSet<>() : new ArrayList<>();
        ReflectiveAccessors.set(setter, owner, created);
        return created;
    }
}
