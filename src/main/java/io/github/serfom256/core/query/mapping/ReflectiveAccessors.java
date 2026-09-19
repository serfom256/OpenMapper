package io.github.serfom256.core.query.mapping;

import io.github.serfom256.exceptions.entity.EntityFieldAccessException;
import io.github.serfom256.exceptions.entity.ObjectCreationException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

final class ReflectiveAccessors {

    private ReflectiveAccessors() {
    }

    static MethodHandle constructor(Class<?> type) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            return lookup(type).unreflectConstructor(constructor)
                    .asType(MethodType.methodType(Object.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new ObjectCreationException(e);
        }
    }

    static boolean hasNoArgConstructor(Class<?> type) {
        try {
            type.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    static Constructor<?> canonicalConstructor(Class<?> type, Class<?>[] fieldTypes) {
        for (Constructor<?> candidate : type.getDeclaredConstructors()) {
            if (Arrays.equals(candidate.getParameterTypes(), fieldTypes)) {
                return candidate;
            }
        }
        return null;
    }

    static MethodHandle spreadingConstructor(Constructor<?> constructor) {
        try {
            int arity = constructor.getParameterCount();
            return lookup(constructor.getDeclaringClass()).unreflectConstructor(constructor)
                    .asType(MethodType.genericMethodType(arity))
                    .asSpreader(Object[].class, arity);
        } catch (IllegalAccessException e) {
            throw new ObjectCreationException(e);
        }
    }

    static Object construct(MethodHandle spreadingConstructor, Object[] arguments) {
        try {
            return spreadingConstructor.invoke(arguments);
        } catch (Throwable t) {
            throw new ObjectCreationException(t);
        }
    }

    static MethodHandle getter(Field field) {
        try {
            return lookup(field.getDeclaringClass()).unreflectGetter(field)
                    .asType(MethodType.methodType(Object.class, Object.class));
        } catch (IllegalAccessException e) {
            throw new EntityFieldAccessException(e);
        }
    }

    static MethodHandle setter(Field field) {
        try {
            return lookup(field.getDeclaringClass()).unreflectSetter(field)
                    .asType(MethodType.methodType(void.class, Object.class, Object.class));
        } catch (IllegalAccessException e) {
            throw new EntityFieldAccessException(e);
        }
    }

    static Object construct(MethodHandle constructor) {
        try {
            return (Object) constructor.invokeExact();
        } catch (Throwable t) {
            throw new ObjectCreationException(t);
        }
    }

    static Object get(MethodHandle getter, Object target) {
        try {
            return (Object) getter.invokeExact(target);
        } catch (Throwable t) {
            throw new EntityFieldAccessException(t);
        }
    }

    static void set(MethodHandle setter, Object target, Object value) {
        try {
            setter.invokeExact(target, value);
        } catch (Throwable t) {
            throw new EntityFieldAccessException(t);
        }
    }

    private static MethodHandles.Lookup lookup(Class<?> owner) throws IllegalAccessException {
        return MethodHandles.privateLookupIn(owner, MethodHandles.lookup());
    }
}
