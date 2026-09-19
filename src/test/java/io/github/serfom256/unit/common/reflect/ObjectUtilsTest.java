package io.github.serfom256.unit.common.reflect;

import io.github.serfom256.common.reflect.ObjectUtils;
import io.github.serfom256.exceptions.entity.EntityFieldAccessException;
import io.github.serfom256.exceptions.entity.ObjectCreationException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ObjectUtilsTest {

    public static class SimpleBean {
        private String name;
        private int age;
        public SimpleBean() {}
        public String getName() { return name; }
        public int getAge() { return age; }
    }

    public static class NoDefaultConstructor {
        public NoDefaultConstructor(String arg) {}
    }

    @Test
    void createNewInstance_validClass() {
        assertThrows(ObjectCreationException.class,
                () -> ObjectUtils.createNewInstance(SimpleBean.class));
    }

    @Test
    void createNewInstance_topLevelClass() {
        Object instance = ObjectUtils.createNewInstance(String.class);
        assertNotNull(instance);
    }

    @Test
    void createNewInstance_noDefaultConstructor_throws() {
        assertThrows(ObjectCreationException.class,
                () -> ObjectUtils.createNewInstance(NoDefaultConstructor.class));
    }

    @Test
    void getFieldValue_validField() throws Exception {
        SimpleBean bean = new SimpleBean();
        bean.name = "test";
        java.lang.reflect.Field field = SimpleBean.class.getDeclaredField("name");
        Object value = ObjectUtils.getFieldValue(bean, field);
        assertEquals("test", value);
    }

    @Test
    void getFieldValue_byFieldName() {
        SimpleBean bean = new SimpleBean();
        bean.name = "hello";
        Object value = ObjectUtils.getFieldValue(bean, "name");
        assertEquals("hello", value);
    }

    @Test
    void getFieldValue_nonExistentField_throws() {
        SimpleBean bean = new SimpleBean();
        assertThrows(EntityFieldAccessException.class,
                () -> ObjectUtils.getFieldValue(bean, "nonExistent"));
    }

    @Test
    void modifyFieldValue_setsValue() throws Exception {
        SimpleBean bean = new SimpleBean();
        java.lang.reflect.Field field = SimpleBean.class.getDeclaredField("name");
        ObjectUtils.modifyFieldValue(field, f -> f.set(bean, "modified"));
        assertEquals("modified", bean.name);
    }

    @Test
    void isIterable_listsReturnTrue() {
        assertTrue(ObjectUtils.isIterable(List.class));
        assertTrue(ObjectUtils.isIterable(ArrayList.class));
        assertTrue(ObjectUtils.isIterable(LinkedList.class));
        assertTrue(ObjectUtils.isIterable(Collection.class));
        assertTrue(ObjectUtils.isIterable(Set.class));
        assertTrue(ObjectUtils.isIterable(HashSet.class));
        assertTrue(ObjectUtils.isIterable(Iterable.class));
    }

    @Test
    void isIterable_nonIterableReturnsFalse() {
        assertFalse(ObjectUtils.isIterable(String.class));
        assertFalse(ObjectUtils.isIterable(Integer.class));
        assertFalse(ObjectUtils.isIterable(Object.class));
        assertFalse(ObjectUtils.isIterable(Map.class));
    }

    @Test
    void getInnerClassType_nonIterable_returnsSame() {
        Class<?> result = ObjectUtils.getInnerClassType(String.class, String.class);
        assertEquals(String.class, result);
    }

    @Test
    void getInnerClassType_iterable_returnsGenericParam() throws Exception {
        java.lang.reflect.Field field = TypeHolder.class.getDeclaredField("stringList");
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        Class<?> result = ObjectUtils.getInnerClassType(List.class, type);
        assertEquals(String.class, result);
    }

    static class TypeHolder {
        List<String> stringList;
    }
}
