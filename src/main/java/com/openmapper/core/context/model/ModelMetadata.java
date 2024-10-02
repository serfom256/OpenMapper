package com.openmapper.core.context.model;

import java.lang.reflect.Field;

public class ModelMetadata {

    private final Field optimisticLockField;

    public ModelMetadata(Field optimisticLockField) {
        this.optimisticLockField = optimisticLockField;
    }

    public Field getOptimisticLockField() {
        return optimisticLockField;
    }
}
