package com.openmapper.mappers.entity;

import com.openmapper.core.annotations.entity.Entity;
import com.openmapper.core.annotations.entity.Joined;
import com.openmapper.core.representation.DependencyGraph;
import com.openmapper.core.representation.Graph;
import com.openmapper.exceptions.entity.EntityFieldAccessException;
import com.openmapper.exceptions.entity.ObjectCreationException;
import com.openmapper.util.ObjectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;

public class ResultSetObjectMapper implements ResultSetMapper {

    private final DependencyGraph dependencyGraph = new DependencyGraph();

    @Override
    public Object extract(Type concreteType, Class<?> actualReturnType, Class<?> returnClass, ResultSet resultSet) throws SQLException, EntityFieldAccessException, ObjectCreationException {
        Graph graph = new Graph();
        while (resultSet.next()) {
            extract(actualReturnType, concreteType, resultSet, graph);
        }
        return dependencyGraph.createDependencyGraph(graph, actualReturnType, null, returnClass);
    }

    private void extract(Class<?> concreteType, Type actualType, ResultSet resultSet, Graph graph) throws SQLException, EntityFieldAccessException, ObjectCreationException {
        concreteType = ObjectUtils.getInnerClassType(concreteType, actualType);
        Object entityInstance = ObjectUtils.createNewInstance(concreteType);
        Field[] declaredFields = entityInstance.getClass().getDeclaredFields();
        Map<Object, Object> entityMap = graph.getIfAbsent(concreteType);
        for (Field f : declaredFields) {
            com.openmapper.core.annotations.entity.Field annotation = f.getAnnotation(com.openmapper.core.annotations.entity.Field.class);
            if (annotation != null) {
                ObjectUtils.modifyFieldValue(f, fld -> fld.set(entityInstance, ResultSetUtils.extractFromResultSet(f.getType(), resultSet, annotation.name())));
            } else if (f.getAnnotation(Joined.class) != null) {
                extract(f.getType(), f.getGenericType(), resultSet, graph);
            }
        }
        entityMap.put(resultSet.getObject(concreteType.getAnnotation(Entity.class).primaryKey()), entityInstance); // was class.getAnnotation()
    }


}
