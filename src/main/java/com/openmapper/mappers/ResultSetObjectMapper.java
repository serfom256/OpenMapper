package com.openmapper.mappers;

import com.openmapper.annotations.entity.Model;
import com.openmapper.annotations.entity.Joined;
import com.openmapper.annotations.entity.Nested;
import com.openmapper.common.reflect.ObjectUtils;
import com.openmapper.core.query.representation.DependencyGraph;
import com.openmapper.core.query.representation.Graph;

import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class ResultSetObjectMapper implements ResultSetMapper {

    private final DependencyGraph dependencyGraph = new DependencyGraph();

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
            Class<?> concreteType,
            Type actualType,
            ResultSet resultSet,
            Graph graph,
            Set<Class<?>> usedTypes) {
        concreteType = ObjectUtils.getInnerClassType(concreteType, actualType);

        if (usedTypes.contains(concreteType))
            return;

        final Object entityInstance = ObjectUtils.createNewInstance(concreteType);
        final Map<Object, Object> entityMap = graph.getIfAbsent(concreteType);

        usedTypes.add(concreteType);

        for (Field f : entityInstance.getClass().getDeclaredFields()) {
            com.openmapper.annotations.entity.Field annotation = f
                    .getAnnotation(com.openmapper.annotations.entity.Field.class);
            if (annotation != null) { // handling fields entities
                final String column = getFieldNameOrDefault(f, annotation.name());
                ObjectUtils.modifyFieldValue(f, fld -> fld.set(entityInstance,
                        ResultSetUtils.extractFromResultSet(f.getType(), resultSet, column)));
            } else if (f.getAnnotation(Joined.class) != null) { // handling joined entities
                extract(f.getType(), f.getGenericType(), resultSet, graph, usedTypes);
            } else if (f.getAnnotation(Nested.class) != null) {// handling nested entities
                ObjectUtils.modifyFieldValue(f,
                        fld -> fld.set(entityInstance, extractNestedEntity(f.getType(), resultSet)));
            }
        }

        final Object primaryKeyValue = ObjectUtils.getFieldValue(entityInstance,
                concreteType.getAnnotation(Model.class).primaryKey());

        entityMap.put(primaryKeyValue, entityInstance);
        usedTypes.remove(concreteType);
    }

    private Object extractNestedEntity(Class<?> concreteType, ResultSet resultSet) {
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
