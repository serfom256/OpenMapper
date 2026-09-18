package io.github.serfom256.core.context.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

import io.github.serfom256.annotations.entity.EnumType;
import io.github.serfom256.annotations.entity.Joined;
import java.util.Set;

public class ModelMetadata {

    private final Field optimisticLockField;
    private final Map<Field, Annotation> annotatedFields;
    private final Set<Field> jsonFields;
    private final Map<Field, Joined> cascadingAssociations;
    private final Set<Field> readOnlyFields;
    private final Map<Field, EnumType> enumTypes;

    public ModelMetadata(Field optimisticLockField, Map<Field, Annotation> annotatedFields) {
        this(optimisticLockField, annotatedFields, Set.of());
    }

    public ModelMetadata(Field optimisticLockField, Map<Field, Annotation> annotatedFields,
                         Set<Field> jsonFields) {
        this(optimisticLockField, annotatedFields, jsonFields, Map.of());
    }

    public ModelMetadata(Field optimisticLockField, Map<Field, Annotation> annotatedFields,
                         Set<Field> jsonFields, Map<Field, Joined> cascadingAssociations) {
        this(optimisticLockField, annotatedFields, jsonFields, cascadingAssociations, Set.of());
    }

    public ModelMetadata(Field optimisticLockField, Map<Field, Annotation> annotatedFields,
                         Set<Field> jsonFields, Map<Field, Joined> cascadingAssociations,
                         Set<Field> readOnlyFields) {
        this(optimisticLockField, annotatedFields, jsonFields, cascadingAssociations, readOnlyFields,
                Map.of());
    }

    public ModelMetadata(Field optimisticLockField, Map<Field, Annotation> annotatedFields,
                         Set<Field> jsonFields, Map<Field, Joined> cascadingAssociations,
                         Set<Field> readOnlyFields, Map<Field, EnumType> enumTypes) {
        this.enumTypes = enumTypes;
        this.optimisticLockField = optimisticLockField;
        this.annotatedFields = annotatedFields;
        this.jsonFields = jsonFields;
        this.cascadingAssociations = cascadingAssociations;
        this.readOnlyFields = readOnlyFields;
    }

    public boolean isReadOnly(Field field) {
        return readOnlyFields.contains(field);
    }

    public EnumType enumTypeOf(Field field) {
        return enumTypes.getOrDefault(field, EnumType.NAME);
    }

    public Map<Field, Joined> getCascadingAssociations() {
        return cascadingAssociations;
    }

    public boolean isJson(Field field) {
        return jsonFields.contains(field);
    }

    public Field getOptimisticLockField() {
        return optimisticLockField;
    }

    public Map<Field, Annotation> getAnnotatedFields() {
        return annotatedFields;
    }
}
