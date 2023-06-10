package com.openmapper.mappers;

import com.openmapper.annotations.entity.Entity;
import com.openmapper.annotations.entity.Joined;
import com.openmapper.annotations.entity.Nested;
import com.openmapper.core.representation.DependencyGraph;
import com.openmapper.core.representation.Graph;
import com.openmapper.common.reflect.ObjectUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;

@Component
public class ResultSetObjectMapper implements ResultSetMapper {

    private final DependencyGraph dependencyGraph = new DependencyGraph();

    @Override
    public Object extract(Type concreteType, Class<?> returnClass, Class<?> actualReturnClass, ResultSet resultSet) throws SQLException {
        Graph graph = new Graph();
        final Set<String> columns = ResultSetUtils.getAvailableColumnNames(resultSet);
        while (resultSet.next()) {
            extract(returnClass, concreteType, resultSet, graph, new HashSet<>(8), columns);
        }
        if (graph.isEmpty()) return null;
        return dependencyGraph.createDependencyGraph(graph, returnClass, actualReturnClass, null, null, new HashSet<>(8));
    }

    private void extract(Class<?> concreteType, Type actualType, ResultSet resultSet, Graph graph, Set<Class<?>> usedTypes, Set<String> columns) {
        concreteType = ObjectUtils.getInnerClassType(concreteType, actualType);
        if (usedTypes.contains(concreteType)) return;
        Object entityInstance = ObjectUtils.createNewInstance(concreteType);
        Map<Object, Object> entityMap = graph.getIfAbsent(concreteType);
        usedTypes.add(concreteType);
        for (Field f : entityInstance.getClass().getDeclaredFields()) {
            com.openmapper.annotations.entity.Field annotation = f.getAnnotation(com.openmapper.annotations.entity.Field.class);
            if (annotation != null) { // handling fields entities
                final String column = getFieldNameOrDefault(f, annotation.name());
                if (!columns.contains(column)) continue;
                ObjectUtils.modifyFieldValue(f, fld -> fld.set(entityInstance, ResultSetUtils.extractFromResultSet(f.getType(), resultSet, column)));
            } else if (f.getAnnotation(Joined.class) != null) { // handling joined entities
                extract(f.getType(), f.getGenericType(), resultSet, graph, usedTypes, columns);
            } else if (f.getAnnotation(Nested.class) != null) {// handling nested entities
                ObjectUtils.modifyFieldValue(f, fld -> fld.set(entityInstance, extractNestedEntity(f.getType(), resultSet, columns)));
            }
        }
        Object primaryKeyValue = ObjectUtils.getFieldValue(entityInstance, concreteType.getAnnotation(Entity.class).primaryKey());
        entityMap.put(primaryKeyValue, entityInstance);
        usedTypes.remove(concreteType);
    }

    private Object extractNestedEntity(Class<?> concreteType, ResultSet resultSet, Set<String> columns) {
        Object entityInstance = ObjectUtils.createNewInstance(concreteType);
        for (Field f : entityInstance.getClass().getDeclaredFields()) {
            com.openmapper.annotations.entity.Field annotation = f.getAnnotation(com.openmapper.annotations.entity.Field.class);
            if (annotation != null) {
                String column = getFieldNameOrDefault(f, annotation.name());
                if (!columns.contains(column)) continue;
                ObjectUtils.modifyFieldValue(f, fld -> fld.set(entityInstance, ResultSetUtils.extractFromResultSet(f.getType(), resultSet, column)));
            }
        }
        return entityInstance;
    }

    public static String getFieldNameOrDefault(Field field, String defaultName) {
        return defaultName.isEmpty() ? field.getName() : defaultName;
    }
}
