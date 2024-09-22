package com.openmapper.core.representation;

import com.openmapper.common.reflect.ObjectUtils;
import com.openmapper.annotations.entity.Joined;
import com.openmapper.exceptions.entity.EntityFieldAccessException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DependencyGraph {

    public Object createDependencyGraph(Graph graph,
            Class<?> thisType,
            Class<?> returnType,
            Object joinedBy,
            String joinedField,
            Set<Class<?>> prevTypes) throws EntityFieldAccessException {
        if (prevTypes.contains(thisType)) {
            return mapRelationShip(graph, thisType, returnType, joinedBy, joinedField);
        }

        final Map<Object, Object> returnObject = graph.get(thisType);

        final Field[] declaredFields = thisType.getDeclaredFields();
        final List<Object> joinedObjects = new ArrayList<>();

        prevTypes.add(thisType);

        for (final Map.Entry<Object, Object> entry : returnObject.entrySet()) {

            final Object entity = entry.getValue();

            for (Field f : declaredFields) {

                final Joined annotation = f.getAnnotation(Joined.class);

                if (annotation != null) {

                    ObjectUtils.modifyFieldValue(f, field -> {
                        final Field thisJoinedField = thisType.getDeclaredField(annotation.joinBy());
                        final Object joinedValue = ObjectUtils.getFieldValue(entity, thisJoinedField);
                        field.set(entity,
                                createDependencyGraph(graph,
                                        ObjectUtils.getInnerClassType(field.getType(), field.getGenericType()),
                                        field.getType(), joinedValue, annotation.to(), prevTypes));
                    });
                }
            }
            if (joinedField != null) {
                if (ObjectUtils.getFieldValue(entity, joinedField).equals(joinedBy)){
                    joinedObjects.add(entity);
                }
            } else {
                joinedObjects.add(entity);
            }
        }

        prevTypes.remove(thisType);

        if (ObjectUtils.isIterable(returnType)) {
            return joinedObjects;
        }

        return joinedObjects.isEmpty() ? null : joinedObjects.get(0);
    }

    private Object mapRelationShip(Graph graph,
            Class<?> thisType,
            Class<?> returnType,
            Object joinedByValue,
            String joinedField) {
        if (ObjectUtils.isIterable(returnType)) {
            return mapManyToManyRelationship(graph, thisType, joinedByValue, joinedField);
        }
        return mapManyToOneRelationship(graph, thisType, joinedByValue, joinedField);
    }

    private List<Object> mapManyToManyRelationship(Graph graph,
            Class<?> thisType,
            Object joinedByValue,
            String joinedField) {
        Map<Object, Object> returnObject = graph.get(thisType);
        final List<Object> result = new ArrayList<>(5);
        for (Map.Entry<Object, Object> entry : returnObject.entrySet()) {
            if (ObjectUtils.getFieldValue(entry.getValue(), joinedField).equals(joinedByValue)) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    private Object mapManyToOneRelationship(Graph graph, Class<?> thisType, Object joinedByValue, String joinedField) {
        Map<Object, Object> returnObject = graph.get(thisType);
        for (Map.Entry<Object, Object> entry : returnObject.entrySet()) {
            if (ObjectUtils.getFieldValue(entry.getValue(), joinedField).equals(joinedByValue)) {
                return entry.getValue();
            }
        }
        return null;
    }

}
