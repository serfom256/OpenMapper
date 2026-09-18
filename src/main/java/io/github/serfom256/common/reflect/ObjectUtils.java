package io.github.serfom256.common.reflect;

import io.github.serfom256.exceptions.entity.EntityFieldAccessException;
import io.github.serfom256.exceptions.entity.ObjectCreationException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ObjectUtils {

    private ObjectUtils() {
    }

    private static final Set<Class<?>> ITERABLE = new HashSet<>();

    static {
        ITERABLE.addAll(Arrays.asList(
                Iterable.class, List.class, ArrayList.class, LinkedList.class,
                Collection.class, Set.class, HashSet.class, LinkedHashSet.class,
                NavigableSet.class, SortedSet.class, TreeSet.class));
    }

    /**
     * Creates new class instance
     */
    public static Object createNewInstance(Class<?> type) throws ObjectCreationException {
        Class<?> clazz;
        try {
            clazz = Class.forName(type.getCanonicalName());
        } catch (ClassNotFoundException e) {
            throw new ObjectCreationException(e);
        }
        try {
            return clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException e) {
            throw new ObjectCreationException(e);
        }
    }

    public static Object getFieldValue(Object object, Field field) throws EntityFieldAccessException {
        Object result;
        field.setAccessible(true);
        try {
            result = field.get(object);
        } catch (Exception e) {
            throw new EntityFieldAccessException(e);
        }
        field.setAccessible(false);
        return result;
    }

    public static Object getFieldValue(Object object, String field) throws EntityFieldAccessException {
        try {
            return getFieldValue(object, object.getClass().getDeclaredField(field));
        } catch (NoSuchFieldException e) {
            throw new EntityFieldAccessException(e);
        }
    }

    public static void modifyFieldValue(Field field, FieldModifier fieldConsumer) throws EntityFieldAccessException {
        field.setAccessible(true);
        try {
            fieldConsumer.modify(field);
        } catch (Exception e) {
            throw new EntityFieldAccessException(e);
        }
        field.setAccessible(false);
    }

    public static boolean isIterable(Class<?> clazz) {
        return ITERABLE.contains(clazz);
    }

    /**
     * Returns iterable generic object's wrapped class type
     *
     * @param clazz concrete class type
     * @param type  expected return type
     * @return unwrapped type
     */
    public static Field[] mappedFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Set<String> declared = new HashSet<>();
        for (Class<?> current = type; current != null && current != Object.class;
                current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!field.isSynthetic() && declared.add(field.getName())) {
                    fields.add(field);
                }
            }
        }
        return fields.toArray(new Field[0]);
    }

    public static Class<?> getInnerClassType(Class<?> clazz, Type type) {
        if (ITERABLE.contains(clazz)) {
            return ((Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0]);
        }
        return clazz;
    }
}
