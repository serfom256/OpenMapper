package com.openmapper.core.context.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

public class ModelMetadata {

    private final Field optimisticLockField;
    private final Map<Field, Annotation> annotatedFields;

    public ModelMetadata(Field optimisticLockField, Map<Field, Annotation> annotatedFields) {
        this.optimisticLockField = optimisticLockField;
        this.annotatedFields = annotatedFields;
    }

    public Field getOptimisticLockField() {
        return optimisticLockField;
    }

    public Map<Field, Annotation> getAnnotatedFields() {
        return annotatedFields;
    }
}
