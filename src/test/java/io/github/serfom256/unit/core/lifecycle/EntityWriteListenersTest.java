package io.github.serfom256.unit.core.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.core.lifecycle.EntityWriteListener;
import io.github.serfom256.core.lifecycle.EntityWriteListeners;
import io.github.serfom256.model.operations.DmlOperation;

class EntityWriteListenersTest {

    interface Auditable {
    }

    static class Product implements Auditable {
    }

    static class Unrelated {
    }

    private final List<String> calls = new ArrayList<>();

    private EntityWriteListener<Auditable> auditListener() {
        return new EntityWriteListener<>() {
            public Class<Auditable> type() {
                return Auditable.class;
            }

            public void beforeWrite(Auditable entity, DmlOperation operation) {
                calls.add(entity.getClass().getSimpleName() + ":" + operation);
            }
        };
    }

    @Test
    @DisplayName("a listener on a base type receives its subtypes")
    void baseTypeCoversSubtypes() {
        new EntityWriteListeners(List.of(auditListener())).beforeWrite(new Product(), DmlOperation.INSERT);
        assertThat(calls).containsExactly("Product:INSERT");
    }

    @Test
    @DisplayName("entities outside the listener's type are untouched")
    void unrelatedTypesAreSkipped() {
        new EntityWriteListeners(List.of(auditListener())).beforeWrite(new Unrelated(), DmlOperation.INSERT);
        assertThat(calls).isEmpty();
    }

    @Test
    @DisplayName("the operation is passed through so inserts and updates can differ")
    void operationIsPassedThrough() {
        new EntityWriteListeners(List.of(auditListener())).beforeWrite(new Product(), DmlOperation.UPDATE);
        assertThat(calls).containsExactly("Product:UPDATE");
    }

    @Test
    @DisplayName("reads never invoke listeners")
    void selectDoesNotInvokeListeners() {
        new EntityWriteListeners(List.of(auditListener())).beforeWrite(new Product(), DmlOperation.SELECT);
        assertThat(calls).isEmpty();
    }

    @Test
    @DisplayName("no listeners registered is a no-op")
    void emptyRegistryIsInert() {
        new EntityWriteListeners().beforeWrite(new Product(), DmlOperation.INSERT);
        assertThat(calls).isEmpty();
    }
}
