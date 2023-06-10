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

    public Object createDependencyGraph(Graph graph, Class<?> thisType, Class<?> returnClass, Object joinedBy, String joinedField, Set<Class<?>> prevTypes) throws EntityFieldAccessException {
        boolean isTransient = prevTypes.contains(thisType);
        Map<Object, Object> returnObject = graph.get(thisType);
        prevTypes.add(thisType);
        Field[] declaredFields = thisType.getDeclaredFields();
        List<Object> joinedObjects = new ArrayList<>();
        for (var entry : returnObject.entrySet()) {
            Object entity = entry.getValue();
            if (!isTransient) {
                for (Field f : declaredFields) {
                    Joined annotation = f.getAnnotation(Joined.class);
                    if (annotation != null) {
                        ObjectUtils.modifyFieldValue(f, field -> {
                            Field thisJoinedField = thisType.getDeclaredField(annotation.joinBy());
                            Object joinedValue = ObjectUtils.getFieldValue(entity, thisJoinedField);
                            field.set(entity, createDependencyGraph(graph, ObjectUtils.getInnerClassType(field.getType(), field.getGenericType()), field.getType(), joinedValue, annotation.to(), prevTypes));
                        });
                    }
                }
            }
            if (joinedField != null) {
                if (ObjectUtils.getFieldValue(entity, joinedField).equals(joinedBy)) joinedObjects.add(entity);
            } else {
                joinedObjects.add(entity);
            }

        }
        if (!isTransient) prevTypes.remove(thisType);
        if (ObjectUtils.isIterable(returnClass)) {
            return joinedObjects;
        }
        return joinedObjects.isEmpty() ? null : joinedObjects.get(0);
    }

}
