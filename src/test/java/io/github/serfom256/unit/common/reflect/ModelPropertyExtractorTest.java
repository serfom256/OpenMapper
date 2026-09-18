package io.github.serfom256.unit.common.reflect;

import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.common.reflect.ModelPropertyExtractor;
import io.github.serfom256.core.lifecycle.EntityIdGenerators;
import io.github.serfom256.core.lifecycle.EntityWriteListeners;
import io.github.serfom256.core.query.handlers.ResultSetHandler;
import io.github.serfom256.core.query.handlers.ResultSetHandlerStrategy;
import io.github.serfom256.core.query.model.QuerySpecifications;
import io.github.serfom256.unit.support.TestPlans;
import io.github.serfom256.mappers.ModelExtractor;
import io.github.serfom256.model.operations.DmlOperation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelPropertyExtractorTest {

    @Mock ModelExtractor modelExtractor;

    @Model(primaryKey = "id")
    public static class TestModel {
        @Field(name = "id") private long id;
        @Field(name = "name") private String name;
        public TestModel() {}
    }

    interface TestDao {
        @DaoMethod(operation = DmlOperation.SELECT)
        String findByParam(@Param(name = "id") long id);

        @DaoMethod(operation = DmlOperation.SELECT)
        String findByTwoParams(@Param(name = "a") int a, @Param(name = "b") String b);

        @DaoMethod(operation = DmlOperation.SELECT)
        String findNoAnnotation(long id);
    }

    @Test
    void extractQuerySpecs_singleParam() throws Exception {
        ModelPropertyExtractor extractor = new ModelPropertyExtractor(modelExtractor, new EntityWriteListeners(), new EntityIdGenerators());
        QuerySpecifications qs = new QuerySpecifications();

        extractor.extractQuerySpecifications(
                TestPlans.planFor(TestDao.class, "findByParam", long.class), new Object[]{42L}, qs);

        assertEquals(42L, qs.getParams().get("id"));
        assertEquals(DmlOperation.SELECT, qs.getOperation());
        assertNotNull(qs.getHandler());
    }

    @Test
    void extractQuerySpecs_multipleParams() throws Exception {
        ModelPropertyExtractor extractor = new ModelPropertyExtractor(modelExtractor, new EntityWriteListeners(), new EntityIdGenerators());
        QuerySpecifications qs = new QuerySpecifications();

        extractor.extractQuerySpecifications(
                TestPlans.planFor(TestDao.class, "findByTwoParams", int.class, String.class),
                new Object[]{10, "hello"}, qs);

        assertEquals(10, qs.getParams().get("a"));
        assertEquals("hello", qs.getParams().get("b"));
    }

    @Test
    void extractQuerySpecs_noParamAnnotation_usesParameterName() throws Exception {
        ModelPropertyExtractor extractor = new ModelPropertyExtractor(modelExtractor, new EntityWriteListeners(), new EntityIdGenerators());
        QuerySpecifications qs = new QuerySpecifications();

        extractor.extractQuerySpecifications(
                TestPlans.planFor(TestDao.class, "findNoAnnotation", long.class), new Object[]{99L}, qs);

        assertNotNull(qs.getParams());
        assertEquals(1, qs.getParams().size());
    }

    @Test
    void extractQuerySpecs_onRetry_isIdempotent() {
        ModelPropertyExtractor extractor = new ModelPropertyExtractor(modelExtractor, new EntityWriteListeners(), new EntityIdGenerators());
        QuerySpecifications qs = new QuerySpecifications();
        qs.setParams(java.util.Map.of("existing", "value"));

        extractor.extractQuerySpecifications(
                TestPlans.planFor(TestDao.class, "findByParam", long.class), new Object[]{42L}, qs);
        extractor.extractQuerySpecifications(
                TestPlans.planFor(TestDao.class, "findByParam", long.class), new Object[]{42L}, qs);

        assertEquals(1, qs.getParams().size());
        assertEquals(42L, qs.getParams().get("id"));
    }
}
