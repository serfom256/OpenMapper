package io.github.serfom256.mappers;

import io.github.serfom256.common.reflect.FieldValueGenerator;
import io.github.serfom256.common.reflect.ModelFieldReader;
import io.github.serfom256.common.reflect.ObjectUtils;
import io.github.serfom256.core.context.ModelMetadataContext;
import io.github.serfom256.core.context.model.ModelMetadata;
import io.github.serfom256.core.query.model.ModelSpecifications;
import io.github.serfom256.core.query.model.QuerySpecifications;
import io.github.serfom256.annotations.entity.Json;
import io.github.serfom256.annotations.entity.Nested;
import io.github.serfom256.annotations.entity.OptimisticLockField;
import io.github.serfom256.exceptions.entity.ModelValidationException;
import io.github.serfom256.json.Serializer;


import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

public class ModelExtractor {

    private final FieldValueGenerator fieldValueGenerator;
    private final ModelMetadataContext modelMetadataContext;
    private final Serializer jsonCodec;
    private final ModelFieldReader fieldReader;
    private final ColumnTypeRegistry columnTypes;

    public ModelExtractor(FieldValueGenerator fieldValueGenerator,
                          ModelMetadataContext modelMetadataContext,
                          @Nullable Serializer jsonCodec,
                          ModelFieldReader fieldReader,
                          ColumnTypeRegistry columnTypes) {
        this.fieldValueGenerator = fieldValueGenerator;
        this.modelMetadataContext = modelMetadataContext;
        this.jsonCodec = jsonCodec;
        this.fieldReader = fieldReader;
        this.columnTypes = columnTypes;
    }

    public ModelSpecifications extractSpecification(Object entity, QuerySpecifications specifications) {
        if (entity == null) {
            return new ModelSpecifications(Collections.emptyMap());
        }

        final ModelMetadata modelMetadata = modelMetadataContext.getMetadataByType(entity.getClass());
        final Map<Field, Annotation> annotatedFields = modelMetadata.getAnnotatedFields();

        final Map<String, Object> values = new HashMap<>();
        final ModelSpecifications modelSpecifications = new ModelSpecifications(values);

        for (Map.Entry<Field, Annotation> entry : annotatedFields.entrySet()) {
            final Class<? extends Annotation> annotationType = entry.getValue().annotationType();
            final Field f = entry.getKey();
            final Annotation annotation = entry.getValue();
            final Object fieldValue = fieldReader.read(entity, f);

            if (annotationType.equals(io.github.serfom256.annotations.entity.Field.class)) {
                if (modelMetadata.isReadOnly(f)) {
                    continue;
                }
                values.put(getFieldName(annotation, f), toBindValue(modelMetadata, f, fieldValue));

            } else if (annotationType.equals(Nested.class)) {
                ModelSpecifications nestedSpec = extractSpecification(fieldValue, specifications);
                values.putAll(nestedSpec.getParams());
                if (specifications.getOperation().advancesVersion()) {
                    mapToModelSpecifications(modelSpecifications, nestedSpec.getOptimisticLockName(),
                            nestedSpec.getGeneratedOptimisticLockValue());
                }

            } else if (annotationType.equals(OptimisticLockField.class)) {
                OptimisticLockField lockField = (OptimisticLockField) annotation;
                String name = getLockFieldName(lockField, f);

                if (specifications.getOperation().advancesVersion()) {
                    Object nextValue = fieldValueGenerator.getNextOptimisticLockFieldValue(fieldValue);

                    mapToModelSpecifications(modelSpecifications, f.getName(), nextValue);
                    values.put(name, nextValue);
                    values.put(lockField.oldVersion(), fieldValue);
                } else {
                    values.put(name, fieldValue);
                }
            }
        }

        return modelSpecifications;
    }

    private Object toBindValue(ModelMetadata modelMetadata, Field field, Object fieldValue) {
        if (fieldValue == null) {
            return null;
        }
        if (!modelMetadata.isJson(field)) {
            return columnTypes.toParameter(fieldValue, modelMetadata.enumTypeOf(field));
        }
        if (jsonCodec == null) {
            throw new ModelValidationException(field.getDeclaringClass(),
                    "field '" + field.getName() + "' is @Json but no JsonCodec is available "
                            + "(add Jackson to the classpath or define a JsonCodec bean)");
        }
        return jsonCodec.serialize(fieldValue);
    }

    private String getFieldName(Annotation annotation, Field field) {
        io.github.serfom256.annotations.entity.Field fieldAnnotation = (io.github.serfom256.annotations.entity.Field) annotation;
        return fieldAnnotation.name().isEmpty() ? field.getName() : fieldAnnotation.name();
    }

    private String getLockFieldName(OptimisticLockField lockField, Field field) {
        return lockField.name().isEmpty() ? field.getName() : lockField.name();
    }

    private void mapToModelSpecifications(
            ModelSpecifications model,
            String optimisticLockName,
            Object lockValue) {
        model.setOptimisticLockName(optimisticLockName);
        model.setGeneratedOptimisticLockValue(lockValue);
    }
}