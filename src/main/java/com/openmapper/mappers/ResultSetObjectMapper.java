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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
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
        final Graph graph = new Graph(getColumnsSet(resultSet));

        while (resultSet.next()) {
            extract(returnClass, concreteType, resultSet, graph, new HashSet<>(8));
        }
        if (graph.isEmpty())
            return null;

        return dependencyGraph.createDependencyGraph(
                graph,
                returnClass,
                actualReturnClass,
                null,
                null,
                new HashSet<>(8));
    }

    private void extract(
            Class<?> targetType,
            Type actualType,
            ResultSet resultSet,
            Graph graph,
            Set<Class<?>> usedTypes) {
        targetType = ObjectUtils.getInnerClassType(targetType, actualType);

        if (usedTypes.contains(targetType)) {
            return;
        }

        Map<Field, Annotation> annotatedFields = getFieldsMap(targetType, graph);
        if (!filterAndCheckMappedFields(annotatedFields, graph)) {
            return;
        }

        final Object entityInstance = ObjectUtils.createNewInstance(targetType);
        final Map<Object, Object> entityMap = graph.getIfAbsent(targetType);

        usedTypes.add(targetType);

        for (Map.Entry<Field, Annotation> entry : annotatedFields.entrySet()) {
            final Class<? extends Annotation> annotationType = entry.getValue().annotationType();
            final Field f = entry.getKey();
            final Annotation annotation = entry.getValue();

            if (annotationType.equals(com.openmapper.annotations.entity.Field.class)) {
                var fieldAnnotation = (com.openmapper.annotations.entity.Field) annotation;
                String column = getFieldNameOrDefault(f, fieldAnnotation.name());
                modifyField(f, entityInstance, resultSet, column);

            } else if (annotationType.equals(OptimisticLockField.class)) {
                String column = getFieldNameOrDefault(f, ((OptimisticLockField) annotation).name());
                modifyField(f, entityInstance, resultSet, column);

            } else if (annotationType.equals(Joined.class)) {
                extract(f.getType(), f.getGenericType(), resultSet, graph, usedTypes);

            } else if (annotationType.equals(Nested.class)) {
                ObjectUtils.modifyFieldValue(f,
                        fld -> fld.set(entityInstance, extractNested(f.getType(), resultSet, graph)));
            }
        }
        String pk = targetType.getAnnotation(Model.class).primaryKey();
        final Object primaryKeyValue = ObjectUtils.getFieldValue(entityInstance, pk);

        entityMap.put(primaryKeyValue, entityInstance);
        usedTypes.remove(targetType);
    }

    private Object extractNested(Class<?> concreteType, ResultSet resultSet, Graph graph) { // TODO fix mapping

        Map<Field, Annotation> annotatedFields = getFieldsMap(concreteType, graph);
        if (!filterAndCheckMappedFields(annotatedFields, graph)) {
            return null;
        }

        final Object entityInstance = ObjectUtils.createNewInstance(concreteType);

        for (Field f : entityInstance.getClass().getDeclaredFields()) {
            var annotation = f.getAnnotation(com.openmapper.annotations.entity.Field.class);
            if (annotation != null) {
                String column = getFieldNameOrDefault(f, annotation.name());
                Object value = ResultSetUtils.extractFromResultSet(f.getType(), resultSet, column);
                ObjectUtils.modifyFieldValue(f, fld -> fld.set(entityInstance, value));
            }
        }

        return entityInstance;
    }

    private void modifyField(Field f, Object entityInstance, ResultSet resultSet, String column) {
        Object valueFromResultSet = ResultSetUtils.extractFromResultSet(f.getType(), resultSet, column);
        ObjectUtils.modifyFieldValue(f, fld -> fld.set(entityInstance, valueFromResultSet));
    }

    private String getFieldNameOrDefault(Field field, String defaultName) {
        return defaultName.isEmpty() ? field.getName() : defaultName;
    }

    private Set<String> getColumnsSet(ResultSet rs) throws SQLException {
        ResultSetMetaData rsMeta = rs.getMetaData();
        int columnCount = rsMeta.getColumnCount();

        Set<String> columnSet = new HashSet<>();
        for (int i = 1; i <= columnCount; i++) {
            columnSet.add(rsMeta.getColumnLabel(i));
        }
        return columnSet;
    }

    private boolean filterAndCheckMappedFields(Map<Field, Annotation> annotatedFields, Graph graph) {
        Iterator<Map.Entry<Field, Annotation>> iterator = annotatedFields.entrySet().iterator();
        boolean shouldMap = false;
        
        while (iterator.hasNext()) {
            Map.Entry<Field, Annotation> e = iterator.next();
            if (e.getValue() instanceof com.openmapper.annotations.entity.Field) {
                var mappedField = (com.openmapper.annotations.entity.Field) e.getValue();
                if (!shouldMap(e.getKey(), mappedField.name(), graph)) {
                    iterator.remove();
                } else {
                    shouldMap = true;
                }
            }
        }
        return shouldMap;
    }

    private boolean shouldMap(Field f, String columnName, Graph graph) {
        return graph.containsColumn(getFieldNameOrDefault(f, columnName));
    }

    private Map<Field, Annotation> getFieldsMap(Class<?> targetType, Graph graph) {
        ModelMetadata modelMetadata = modelMetadataContext.getMetadataByType(targetType);
        return modelMetadata.getAnnotatedFields();
    }
}
