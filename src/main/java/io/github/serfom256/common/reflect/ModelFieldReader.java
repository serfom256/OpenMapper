package io.github.serfom256.common.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.serfom256.exceptions.entity.EntityFieldAccessException;

/**
 * Reads mapped field values off an entity when building a statement's bind
 * parameters.
 *
 * <p>Each field is resolved to a {@link MethodHandle} once and reused, mirroring
 * what {@link io.github.serfom256.core.query.mapping.MappingPlan} does on the read
 * side. The previous approach called {@code setAccessible(true)}, read the
 * value, then {@code setAccessible(false)} on every field of every entity of
 * every write. Besides the cost, {@code setAccessible} mutates state shared
 * across threads, so two threads writing the same entity type could clear the
 * flag out from under each other.
 */
public class ModelFieldReader {

    private final Map<Field, MethodHandle> getters = new ConcurrentHashMap<>();

    public Object read(Object entity, Field field) {
        try {
            return getters.computeIfAbsent(field, this::getterFor).invoke(entity);
        } catch (Throwable t) {
            throw new EntityFieldAccessException(t);
        }
    }

    private MethodHandle getterFor(Field field) {
        try {
            return MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup())
                    .unreflectGetter(field)
                    .asType(MethodType.methodType(Object.class, Object.class));
        } catch (IllegalAccessException e) {
            throw new EntityFieldAccessException(e);
        }
    }
}
