package com.openmapper.common;

import com.openmapper.exceptions.entity.EntityFieldAccessException;

import java.lang.reflect.Field;

@FunctionalInterface
public interface FieldModifier {
    void modify(Field field) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, EntityFieldAccessException;
}

