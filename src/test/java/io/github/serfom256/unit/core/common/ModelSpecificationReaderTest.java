package io.github.serfom256.unit.core.common;

import io.github.serfom256.annotations.entity.*;
import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.core.common.ModelSpecificationReader;
import io.github.serfom256.core.context.model.ModelMetadata;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModelSpecificationReaderTest {

    private final ModelSpecificationReader reader = new ModelSpecificationReader();

    @Model(primaryKey = "id")
    static class SimpleModel {
        @Field(name = "id")
        private long id;
        @Field(name = "name")
        private String name;
    }

    @Model(primaryKey = "id")
    static class ModelWithJoined {
        @Field(name = "id")
        private long id;
        @Joined(joinBy = "id", to = "parentId")
        private List<SimpleModel> children;
    }

    @Model(primaryKey = "id")
    static class ModelWithOptLock {
        @Field(name = "id")
        private long id;
        @OptimisticLockField(name = "version", oldVersion = "oldVersion")
        private int version;
    }

    @Model(primaryKey = "id")
    static class ModelWithTwoOptLocks {
        @Field(name = "id")
        private long id;
        @OptimisticLockField(name = "v1", oldVersion = "old1")
        private int v1;
        @OptimisticLockField(name = "v2", oldVersion = "old2")
        private int v2;
    }

    @Dto
    static class DtoModel {
        @Field(name = "name")
        private String name;
    }

    @Model(primaryKey = "id")
    static class ModelWithNested {
        @Field(name = "id")
        private long id;
        @Nested
        private DtoModel nested;
    }

    @Test
    void readSimpleModel_findsAllFields() {
        ModelMetadata metadata = reader.readModelMetadata(SimpleModel.class);
        assertNotNull(metadata.getAnnotatedFields());
        assertEquals(2, metadata.getAnnotatedFields().size());
        assertNull(metadata.getOptimisticLockField());
    }

    @Test
    void readModelWithJoined_findsJoinedField() {
        ModelMetadata metadata = reader.readModelMetadata(ModelWithJoined.class);
        assertEquals(2, metadata.getAnnotatedFields().size());
    }

    @Test
    void readModelWithOptimisticLock_findsLockField() {
        ModelMetadata metadata = reader.readModelMetadata(ModelWithOptLock.class);
        assertNotNull(metadata.getOptimisticLockField());
        assertEquals("version", metadata.getOptimisticLockField().getName());
    }

    @Test
    void readModelWithTwoOptimisticLockFields_throws() {
        assertThrows(io.github.serfom256.exceptions.entity.ModelValidationException.class,
                () -> reader.readModelMetadata(ModelWithTwoOptLocks.class));
    }

    @Test
    void readDto_noOptimisticLockValidation() {
        ModelMetadata metadata = reader.readModelMetadata(DtoModel.class);
        assertNotNull(metadata.getAnnotatedFields());
        assertNull(metadata.getOptimisticLockField());
    }

    @Test
    void readModelWithNested_findsNestedAnnotation() {
        ModelMetadata metadata = reader.readModelMetadata(ModelWithNested.class);
        assertEquals(2, metadata.getAnnotatedFields().size());
        boolean hasNested = metadata.getAnnotatedFields().values().stream()
                .anyMatch(a -> a.annotationType().equals(Nested.class));
        assertTrue(hasNested);
    }

    @Test
    void readModel_fieldAnnotationNames() {
        ModelMetadata metadata = reader.readModelMetadata(SimpleModel.class);
        for (var entry : metadata.getAnnotatedFields().entrySet()) {
            Annotation ann = entry.getValue();
            if (ann instanceof Field fieldAnn) {
                assertTrue(fieldAnn.name().equals("id") || fieldAnn.name().equals("name"));
            }
        }
    }
}
