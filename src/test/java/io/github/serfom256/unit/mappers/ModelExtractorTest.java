package io.github.serfom256.unit.mappers;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.annotations.entity.Nested;
import io.github.serfom256.annotations.entity.OptimisticLockField;
import io.github.serfom256.common.reflect.FieldValueGenerator;
import io.github.serfom256.common.reflect.ModelFieldReader;
import io.github.serfom256.mappers.ColumnTypeRegistry;
import io.github.serfom256.core.common.ModelSpecificationReader;
import io.github.serfom256.core.context.ModelMetadataContext;
import io.github.serfom256.core.context.model.ModelMetadata;
import io.github.serfom256.core.query.model.ModelSpecifications;
import io.github.serfom256.core.query.model.QuerySpecifications;
import io.github.serfom256.mappers.ModelExtractor;
import io.github.serfom256.model.operations.DmlOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ModelExtractorTest {

    private ModelExtractor extractor;
    private ModelMetadataContext metadataContext;

    @Model(primaryKey = "id")
    public static class SimpleEntity {
        @Field(name = "id")
        private long id;
        @Field(name = "name")
        private String name;

        public SimpleEntity() {}
        public SimpleEntity(long id, String name) { this.id = id; this.name = name; }
    }

    @Model(primaryKey = "id")
    public static class EntityWithLock {
        @Field(name = "id")
        private long id;
        @Field(name = "name")
        private String name;
        @OptimisticLockField(name = "version", oldVersion = "oldVersion")
        private int version;

        public EntityWithLock() {}
        public EntityWithLock(long id, String name, int version) {
            this.id = id; this.name = name; this.version = version;
        }
    }

    @BeforeEach
    void setUp() {
        metadataContext = new ModelMetadataContext();
        ModelSpecificationReader reader = new ModelSpecificationReader();
        metadataContext.registerModel(SimpleEntity.class, reader.readModelMetadata(SimpleEntity.class));
        metadataContext.registerModel(EntityWithLock.class, reader.readModelMetadata(EntityWithLock.class));
        extractor = new ModelExtractor(new FieldValueGenerator(), metadataContext, null,
                new ModelFieldReader(), new ColumnTypeRegistry());
    }

    @Test
    void extractSpecification_simpleEntity_extractsFieldValues() {
        SimpleEntity entity = new SimpleEntity(1L, "Alice");
        QuerySpecifications qs = new QuerySpecifications();
        qs.setOperation(DmlOperation.SELECT);

        ModelSpecifications result = extractor.extractSpecification(entity, qs);

        Map<String, Object> params = result.getParams();
        assertEquals(1L, params.get("id"));
        assertEquals("Alice", params.get("name"));
    }

    @Test
    void extractSpecification_nullEntity_returnsEmptyParams() {
        QuerySpecifications qs = new QuerySpecifications();
        ModelSpecifications result = extractor.extractSpecification(null, qs);
        assertTrue(result.getParams().isEmpty());
    }

    @Test
    void extractSpecification_entityWithLock_onUpdate_generatesNewVersion() {
        EntityWithLock entity = new EntityWithLock(1L, "Bob", 3);
        QuerySpecifications qs = new QuerySpecifications();
        qs.setOperation(DmlOperation.UPDATE);

        ModelSpecifications result = extractor.extractSpecification(entity, qs);

        Map<String, Object> params = result.getParams();
        assertEquals(1L, params.get("id"));
        assertEquals("Bob", params.get("name"));
        assertEquals(4, params.get("version"));
        assertEquals(3, params.get("oldVersion"));
        assertNotNull(result.getOptimisticLockName());
    }

    @Test
    void extractSpecification_entityWithLock_onSelect_noVersionGeneration() {
        EntityWithLock entity = new EntityWithLock(1L, "Bob", 3);
        QuerySpecifications qs = new QuerySpecifications();
        qs.setOperation(DmlOperation.SELECT);

        ModelSpecifications result = extractor.extractSpecification(entity, qs);

        Map<String, Object> params = result.getParams();
        assertEquals(3, params.get("version"));
        assertNull(params.get("oldVersion"));
    }
}
