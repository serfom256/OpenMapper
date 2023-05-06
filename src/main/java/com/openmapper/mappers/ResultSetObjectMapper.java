package com.openmapper.mappers;

import com.openmapper.core.annotations.entity.Entity;
import com.openmapper.core.annotations.entity.Joined;
import com.openmapper.core.annotations.entity.Nested;
import com.openmapper.core.representation.DependencyGraph;
import com.openmapper.core.representation.Graph;
import com.openmapper.common.ObjectUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;

@Component
public class ResultSetObjectMapper implements ResultSetMapper {

    private final DependencyGraph dependencyGraph = new DependencyGraph();

    @Override
    public Object extract(Type concreteType, Class<?> actualReturnType, Class<?> returnClass, ResultSet resultSet) throws SQLException {
        Graph graph = new Graph();
        while (resultSet.next()) {
            extract(actualReturnType, concreteType, resultSet, graph);
        }
        return dependencyGraph.createDependencyGraph(graph, actualReturnType, returnClass, null, null);
    }

    private void extract(Class<?> concreteType, Type actualType, ResultSet resultSet, Graph graph) throws SQLException {
        concreteType = ObjectUtils.getInnerClassType(concreteType, actualType);
        Object entityInstance = ObjectUtils.createNewInstance(concreteType);
        Map<Object, Object> entityMap = graph.getIfAbsent(concreteType);
        for (Field f : entityInstance.getClass().getDeclaredFields()) {
            com.openmapper.core.annotations.entity.Field annotation = f.getAnnotation(com.openmapper.core.annotations.entity.Field.class);
            if (annotation != null) {
                ObjectUtils.modifyFieldValue(f, fld -> fld.set(entityInstance, ResultSetUtils.extractFromResultSet(f.getType(), resultSet, getFieldNameOrDefault(f, annotation.name()))));
            } else if (f.getAnnotation(Joined.class) != null) {
                extract(f.getType(), f.getGenericType(), resultSet, graph);
            } else if (f.getAnnotation(Nested.class) != null) {
                ObjectUtils.modifyFieldValue(f, fld -> fld.set(entityInstance, extractNestedEntity(f.getType(), resultSet)));
            }
        }
        Object primaryKeyValue = ObjectUtils.getFieldValue(entityInstance, concreteType.getAnnotation(Entity.class).primaryKey());
        entityMap.put(primaryKeyValue, entityInstance);
    }

    private Object extractNestedEntity(Class<?> concreteType, ResultSet resultSet) {
        Object entityInstance = ObjectUtils.createNewInstance(concreteType);
        for (Field f : entityInstance.getClass().getDeclaredFields()) {
            com.openmapper.core.annotations.entity.Field annotation = f.getAnnotation(com.openmapper.core.annotations.entity.Field.class);
            if (annotation != null) {
                ObjectUtils.modifyFieldValue(f, fld -> fld.set(entityInstance, ResultSetUtils.extractFromResultSet(f.getType(), resultSet, getFieldNameOrDefault(f, annotation.name()))));
            }
        }
        return entityInstance;
    }

    public static String getFieldNameOrDefault(Field field, String defaultName) {
        return defaultName.isEmpty() ? field.getName() : defaultName;
    }
}
