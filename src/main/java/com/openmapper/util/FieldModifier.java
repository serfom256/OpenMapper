package com.openmapper.util;

import com.openmapper.exceptions.EntityFieldAccessException;

import java.lang.reflect.Field;

@FunctionalInterface
public interface FieldModifier {
    void modify(Field field) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, EntityFieldAccessException;
}

