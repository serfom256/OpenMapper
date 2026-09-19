package io.github.serfom256.common.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.serfom256.exceptions.entity.EntityFieldAccessException;

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
