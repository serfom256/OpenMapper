package io.github.serfom256.unit.lifecycle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.common.reflect.ModelFieldReader;
import io.github.serfom256.core.lifecycle.EntityIdGenerators;
import io.github.serfom256.core.lifecycle.IdGenerator;
import io.github.serfom256.exceptions.entity.ModelValidationException;
import io.github.serfom256.model.operations.DmlOperation;

class EntityIdGeneratorsTest {

    @Model(primaryKey = "id")
    public static class Base {

        @Field(name = "id")
        protected Long id;

        public Long getId() {
            return id;
        }
    }

    public static class Derived extends Base {
    }

    @Model(primaryKey = "id")
    public static class PrimitiveKey {

        @Field(name = "id")
        long id;
    }

    private EntityIdGenerators generators(IdGenerator<?>... generators) {
        return new EntityIdGenerators(List.of(generators), new ModelFieldReader());
    }

    private <T> IdGenerator<T> generator(Class<T> type, Object key) {
        return new IdGenerator<>() {
            public Class<T> type() {
                return type;
            }

            public Object generate(T entity) {
                return key;
            }
        };
    }

    @Test
    @DisplayName("a generator declared on a base type covers its subtypes")
    void baseTypeGeneratorCoversSubtypes() {
        Derived entity = new Derived();
        generators(generator(Base.class, 7L)).generateId(entity, DmlOperation.INSERT);
        assertEquals(7L, entity.getId());
    }

    @Test
    @DisplayName("the most specific generator wins when two apply")
    void mostSpecificGeneratorWins() {
        Derived entity = new Derived();
        generators(generator(Base.class, 1L), generator(Derived.class, 2L))
                .generateId(entity, DmlOperation.INSERT);
        assertEquals(2L, entity.getId());
    }

    @Test
    @DisplayName("only inserts generate a key")
    void onlyInsertsGenerate() {
        Base entity = new Base();
        generators(generator(Base.class, 5L)).generateId(entity, DmlOperation.UPDATE);
        assertNull(entity.getId());
    }

    @Test
    @DisplayName("a zero primitive key counts as unset")
    void zeroPrimitiveKeyIsUnset() {
        PrimitiveKey entity = new PrimitiveKey();
        generators(generator(PrimitiveKey.class, 42L)).generateId(entity, DmlOperation.INSERT);
        assertEquals(42L, entity.id);
    }

    @Test
    @DisplayName("a key of the wrong type fails naming the generator and both types")
    void wrongKeyTypeFailsClearly() {
        Base entity = new Base();
        ModelValidationException failure = assertThrows(ModelValidationException.class,
                () -> generators(generator(Base.class, "not-a-long"))
                        .generateId(entity, DmlOperation.INSERT));

        assertTrue(failure.getMessage().contains("java.lang.String"), failure.getMessage());
        assertTrue(failure.getMessage().contains("java.lang.Long"), failure.getMessage());
    }

    @Test
    @DisplayName("with no generators registered nothing happens")
    void noGeneratorsIsANoOp() {
        Base entity = new Base();
        generators().generateId(entity, DmlOperation.INSERT);
        assertNull(entity.getId());
    }
}
