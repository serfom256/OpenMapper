package io.github.serfom256.common.reflect;

import io.github.serfom256.exceptions.entity.EntityFieldAccessException;

import java.lang.reflect.Field;

@FunctionalInterface
public interface FieldModifier {
    void modify(final Field field) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, EntityFieldAccessException;
}

