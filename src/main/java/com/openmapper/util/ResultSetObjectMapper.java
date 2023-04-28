package com.openmapper.util;

import com.openmapper.core.annotations.entity.Entity;
import com.openmapper.core.annotations.entity.Joined;
import com.openmapper.core.view.DependencyGraph;
import com.openmapper.exceptions.EntityFieldAccessException;
import com.openmapper.exceptions.ObjectCreationException;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;

public class ResultSetObjectMapper {


    private static final DependencyGraph dependencyGraph = new DependencyGraph();

    private ResultSetObjectMapper() {
    }

    public static Object extractResult(Type concreteType, Class<?> actualReturnType, Class<?> returnClass, ResultSet resultSet) throws SQLException, EntityFieldAccessException, ObjectCreationException {
        Map<Class<?>, Map<Object, Object>> entrySet = new HashMap<>(4);
        while (resultSet.next()) {
            extract(actualReturnType, concreteType, resultSet, entrySet);
        }
        return dependencyGraph.createDependencyGraph(entrySet, actualReturnType, null, returnClass);
    }

    private static void extract(Class<?> concreteType, Type actualType, ResultSet resultSet, Map<Class<?>, Map<Object, Object>> map) throws SQLException, EntityFieldAccessException, ObjectCreationException {
        concreteType = ObjectUtils.getInnerClassType(concreteType, actualType);
        Object entityInstance = ObjectUtils.createNewInstance(concreteType);
        Field[] declaredFields = entityInstance.getClass().getDeclaredFields();
        Map<Object, Object> entityMap = map.computeIfAbsent(concreteType, m -> new HashMap<>());
        for (Field f : declaredFields) {
            com.openmapper.core.annotations.entity.Field annotation = f.getAnnotation(com.openmapper.core.annotations.entity.Field.class);
            if (annotation != null) {
                ObjectUtils.modifyFieldValue(f, fld -> fld.set(entityInstance, ResultSetUtils.extractFromResultSet(f.getType(), resultSet, annotation.name())));
            } else if (f.getAnnotation(Joined.class) != null) {
                extract(f.getType(), f.getGenericType(), resultSet, map);
            }
        }
        entityMap.put(resultSet.getObject(concreteType.getAnnotation(Entity.class).primaryKey()), entityInstance); // was class.getAnnotation()
    }


}
