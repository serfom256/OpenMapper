package com.openmapper.core.view;

import com.openmapper.core.annotations.entity.Entity;
import com.openmapper.core.annotations.entity.Joined;
import com.openmapper.exceptions.EntityFieldAccessException;
import com.openmapper.util.ObjectUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DependencyGraph {

    public Object createDependencyGraph(Map<Class<?>, Map<Object, Object>> map, Class<?> thisType, Object joinedBy, Class<?> returnClass) throws EntityFieldAccessException {
        Map<Object, Object> returnObject = map.get(thisType);
        Field[] declaredFields = thisType.getDeclaredFields();
        List<Object> joinedObjects = new ArrayList<>();
        for (var entry : returnObject.entrySet()) {
            Object entity = entry.getValue();
            for (Field f : declaredFields) {
                Joined annotation = f.getAnnotation(Joined.class);
                if (annotation != null) {
                    ObjectUtils.modifyFieldValue(f, field -> {
                        Field joinedField = thisType.getDeclaredField(annotation.joinBy());
                        Object joinedValue = ObjectUtils.getFieldValue(entity, joinedField);
                        field.set(entity, createDependencyGraph(map, ObjectUtils.getInnerClassType(field.getType(), field.getGenericType()), joinedValue, field.getType()));
                    });
                }
            }
            String joinToken = entity.getClass().getAnnotation(Entity.class).joinedBy();
            if (!joinToken.isEmpty()) {
                Field joinedField;
                try {
                    joinedField = entity.getClass().getDeclaredField(joinToken);
                } catch (NoSuchFieldException e) {
                    throw new EntityFieldAccessException(e);
                }
                if (ObjectUtils.getFieldValue(entity, joinedField).equals(joinedBy)) {
                    joinedObjects.add(entity);
                }
            } else {
                joinedObjects.add(entity);
            }

        }
        if (ObjectUtils.isIterable(returnClass)) {
            return joinedObjects;
        }
        return joinedObjects.isEmpty() ? null : joinedObjects.get(0);
    }

}
