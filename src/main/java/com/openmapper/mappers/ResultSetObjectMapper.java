package com.openmapper.mappers;

import com.openmapper.annotations.entity.Model;
import com.openmapper.annotations.entity.Joined;
import com.openmapper.annotations.entity.Nested;
import com.openmapper.annotations.entity.OptimisticLockField;
import com.openmapper.common.reflect.ObjectUtils;
import com.openmapper.core.context.ModelMetadataContext;
import com.openmapper.core.context.model.ModelMetadata;
import com.openmapper.core.query.representation.DependencyGraph;
import com.openmapper.core.query.representation.Graph;

import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class ResultSetObjectMapper implements ResultSetMapper {

    private final DependencyGraph dependencyGraph;
    private final ModelMetadataContext modelMetadataContext;

    public ResultSetObjectMapper(DependencyGraph dependencyGraph, ModelMetadataContext modelMetadataContext) {
        this.dependencyGraph = dependencyGraph;
        this.modelMetadataContext = modelMetadataContext;
    }

    @Override
    public Object extract(Type concreteType, Class<?> returnClass, Class<?> actualReturnClass, ResultSet resultSet)
            throws SQLException {
        final Graph graph = new Graph();

        while (resultSet.next()) {
            extract(returnClass, concreteType, resultSet, graph, new HashSet<>(8));
        }
        if (graph.isEmpty())
            return null;

        return dependencyGraph.createDependencyGraph(graph, returnClass, actualReturnClass, null, null,
                new HashSet<>(8));
    }

    private void extract(
            Class<?> targetType,
            Type actualType,
            ResultSet resultSet,
            Graph graph,
            Set<Class<?>> usedTypes) {
        targetType = ObjectUtils.getInnerClassType(targetType, actualType);

        if (usedTypes.contains(targetType))
            return;

        final Object entityInstance = ObjectUtils.createNewInstance(targetType);
        final Map<Object, Object> entityMap = graph.getIfAbsent(targetType);

        usedTypes.add(targetType);

        ModelMetadata modelMetadata = modelMetadataContext.getMetadataByType(targetType);

        Map<Field, Annotation> annotatedFields = modelMetadata.getAnnotatedFields();

        for (Map.Entry<Field, Annotation> entry : annotatedFields.entrySet()) {
            final Class<? extends Annotation> annotationType = entry.getValue().annotationType();
            final Field f = entry.getKey();
            final Annotation annotation = entry.getValue();

            if (annotationType.equals(com.openmapper.annotations.entity.Field.class)) {
                com.openmapper.annotations.entity.Field fieldAnnotation = (com.openmapper.annotations.entity.Field) annotation;
                final String column = getFieldNameOrDefault(f, fieldAnnotation.name());
                modifyField(f, entityInstance, resultSet, column);

            } else if (annotationType.equals(OptimisticLockField.class)) {
                final String column = getFieldNameOrDefault(f, ((OptimisticLockField) annotation).name());
                modifyField(f, entityInstance, resultSet, column);

            } else if (annotationType.equals(Joined.class)) {
                extract(f.getType(), f.getGenericType(), resultSet, graph, usedTypes);
                
            } else if (annotationType.equals(Nested.class)) {
                ObjectUtils.modifyFieldValue(f,
                        fld -> fld.set(entityInstance, extractNestedEntity(f.getType(), resultSet)));
            }
        }

        final Object primaryKeyValue = ObjectUtils.getFieldValue(entityInstance,
                targetType.getAnnotation(Model.class).primaryKey());

        entityMap.put(primaryKeyValue, entityInstance);
        usedTypes.remove(targetType);
    }

    private void modifyField(Field f, Object entityInstance, ResultSet resultSet, String column) {
        Object valueFromResultSet = ResultSetUtils.extractFromResultSet(f.getType(), resultSet, column);
        ObjectUtils.modifyFieldValue(f, fld -> fld.set(entityInstance, valueFromResultSet));
    }

    private Object extractNestedEntity(Class<?> concreteType, ResultSet resultSet) { // TODO fix mapping
        final Object entityInstance = ObjectUtils.createNewInstance(concreteType);

        for (Field f : entityInstance.getClass().getDeclaredFields()) {
            com.openmapper.annotations.entity.Field annotation = f
                    .getAnnotation(com.openmapper.annotations.entity.Field.class);
            if (annotation != null) {
                final String column = getFieldNameOrDefault(f, annotation.name());
                ObjectUtils.modifyFieldValue(f, fld -> fld.set(entityInstance,
                        ResultSetUtils.extractFromResultSet(f.getType(), resultSet, column)));
            }
        }

        return entityInstance;
    }

    private String getFieldNameOrDefault(Field field, String defaultName) {
        return defaultName.isEmpty() ? field.getName() : defaultName;
    }
}
