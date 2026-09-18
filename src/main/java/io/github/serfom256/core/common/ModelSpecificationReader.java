package io.github.serfom256.core.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.serfom256.annotations.entity.Dto;
import io.github.serfom256.annotations.entity.EnumType;
import io.github.serfom256.annotations.entity.Json;
import io.github.serfom256.annotations.entity.Joined;
import io.github.serfom256.annotations.entity.Nested;
import io.github.serfom256.annotations.entity.OptimisticLockField;
import io.github.serfom256.common.reflect.FieldValueGenerator;
import io.github.serfom256.common.reflect.ObjectUtils;
import io.github.serfom256.core.context.model.ModelMetadata;
import io.github.serfom256.exceptions.entity.ModelValidationException;

public class ModelSpecificationReader {

    private final Set<Class<? extends Annotation>> mappableFields = Set.of(
            io.github.serfom256.annotations.entity.Field.class,
            Dto.class,
            Joined.class,
            Nested.class,
            OptimisticLockField.class);

    public ModelMetadata readModelMetadata(Class<?> modelClass) {

        Field[] declaredFields = ObjectUtils.mappedFields(modelClass);

        Map<Field, Annotation> mappedFields = findMappedFields(declaredFields);
        Field optimisticLockField = findOptimisticLockField(declaredFields, modelClass);

        return new ModelMetadata(optimisticLockField, mappedFields, findJsonFields(declaredFields),
                findCascadingAssociations(declaredFields), findReadOnlyFields(declaredFields),
                findEnumTypes(declaredFields));
    }

    private Map<Field, EnumType> findEnumTypes(Field[] declaredFields) {
        Map<Field, EnumType> enumTypes = new HashMap<>();
        for (Field field : declaredFields) {
            io.github.serfom256.annotations.entity.Field column =
                    field.getDeclaredAnnotation(io.github.serfom256.annotations.entity.Field.class);
            if (column != null && field.getType().isEnum()) {
                enumTypes.put(field, column.enumType());
            }
        }
        return enumTypes;
    }

    private Set<Field> findReadOnlyFields(Field[] declaredFields) {
        Set<Field> readOnly = new HashSet<>();
        for (Field field : declaredFields) {
            io.github.serfom256.annotations.entity.Field column =
                    field.getDeclaredAnnotation(io.github.serfom256.annotations.entity.Field.class);
            if (column != null && column.readOnly()) {
                readOnly.add(field);
            }
        }
        return readOnly;
    }

    private Map<Field, Joined> findCascadingAssociations(Field[] declaredFields) {
        Map<Field, Joined> cascading = new HashMap<>();
        for (Field field : declaredFields) {
            Joined joined = field.getDeclaredAnnotation(Joined.class);
            if (joined != null && !joined.saveProcedure().isEmpty()) {
                cascading.put(field, joined);
            }
        }
        return cascading;
    }

    private Set<Field> findJsonFields(Field[] declaredFields) {
        Set<Field> jsonFields = new HashSet<>();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Json.class)) {
                jsonFields.add(field);
            }
        }
        return jsonFields;
    }

    private Field findOptimisticLockField(Field[] declaredFields, Class<?> modelClass) {
        int optimisticLockFieldCount = 0;
        Field optimisticLockField = null;

        if (modelClass.getAnnotation(Dto.class) != null) {
            return optimisticLockField;
        }

        for (Field field : declaredFields) {
            if (field.getDeclaredAnnotation(OptimisticLockField.class) != null) {
                requireSupportedLockType(field, modelClass);
                optimisticLockField = field;
                optimisticLockFieldCount++;
            }
        }
        if (optimisticLockFieldCount > 1) {
            throw new ModelValidationException(modelClass,
                    "invalid @OptimisticLockField field count: expected 1, got %s"
                            .formatted(optimisticLockFieldCount));
        }

        return optimisticLockField;
    }

    private void requireSupportedLockType(Field field, Class<?> modelClass) {
        if (!FieldValueGenerator.supportsOptimisticLockType(field.getType())) {
            throw new ModelValidationException(modelClass,
                    "@OptimisticLockField '%s' is of unsupported type %s: expected one of %s"
                            .formatted(field.getName(), field.getType().getSimpleName(),
                                    FieldValueGenerator.supportedOptimisticLockTypes()));
        }
    }

    private Map<Field, Annotation> findMappedFields(Field[] declaredFields) {
        Map<Field, Annotation> modelDeclaredFieldAnnotations = new HashMap<>();
        for (Field field : declaredFields) {
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (mappableFields.contains(annotation.annotationType())) {
                    modelDeclaredFieldAnnotations.put(field, annotation);
                }
            }
        }

        return modelDeclaredFieldAnnotations;
    }
}
