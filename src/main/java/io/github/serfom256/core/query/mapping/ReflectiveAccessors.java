package io.github.serfom256.core.query.mapping;

import io.github.serfom256.exceptions.entity.EntityFieldAccessException;
import io.github.serfom256.exceptions.entity.ObjectCreationException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Builds and invokes {@link MethodHandle}s for entity construction and field
 * access.
 *
 * <p>Handles are created once (at plan-build time) and normalised to a generic
 * {@code () -> Object}, {@code (Object) -> Object} or {@code (Object, Object) -> void}
 * signature, so the per-row mapping code can invoke them with {@code invokeExact}
 * without any reflection or {@code setAccessible} toggling at runtime.
 */
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

    /**
     * The constructor whose parameters correspond, in order and by type, to the
     * type's instance fields -- the canonical constructor of a record or the
     * primary constructor of a Kotlin data class. Returns {@code null} when no
     * constructor matches.
     */
    static Constructor<?> canonicalConstructor(Class<?> type, Class<?>[] fieldTypes) {
        for (Constructor<?> candidate : type.getDeclaredConstructors()) {
            if (Arrays.equals(candidate.getParameterTypes(), fieldTypes)) {
                return candidate;
            }
        }
        return null;
    }

    /** A handle taking one {@code Object[]} of arguments, for canonical construction. */
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
