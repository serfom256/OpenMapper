package com.openmapper.common.reflect;

import com.openmapper.exceptions.entity.EntityFieldAccessException;

import java.lang.reflect.Field;

@FunctionalInterface
public interface FieldModifier {
    void modify(final Field field) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, EntityFieldAccessException;
}

