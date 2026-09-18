package io.github.serfom256.unit.core.context;

import io.github.serfom256.core.context.DaoMethodContext;
import io.github.serfom256.core.context.ModelMetadataContext;
import io.github.serfom256.core.context.OpenMapperSQLContext;
import io.github.serfom256.unit.support.TestPlans;
import io.github.serfom256.core.context.model.ModelMetadata;
import io.github.serfom256.core.common.DaoMethodFacade;
import io.github.serfom256.exceptions.common.FunctionNotFoundException;
import io.github.serfom256.parser.model.SQLRecord;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContextTest {

    @Nested
    class DaoMethodContextTest {

        private final DaoMethodContext ctx = new DaoMethodContext();

        @Test
        void registerAndLookupProcedure() {
            SQLRecord record = new SQLRecord(List.of(), Map.of());
            ctx.registerSqlProcedure("findById", record);
            assertSame(record, ctx.getSqlRecordByProcedure("findById"));
        }

        @Test
        void lookupUnregisteredProcedure_returnsNull() {
            assertNull(ctx.getSqlRecordByProcedure("nonExistent"));
        }

        @Test
        void registerMethod_thenHasEmbeddedProcedure() throws Exception {
            interface TestDao { void query(@io.github.serfom256.annotations.entity.Sql String sql); }
            Method m = TestDao.class.getDeclaredMethod("query", String.class);
            ctx.registerMethod(m);
            assertTrue(ctx.hasEmbeddedSqlProcedure(m));
        }

        @Test
        void unregisteredMethod_hasNoEmbeddedProcedure() throws Exception {
            interface TestDao { void other(int x); }
            Method m = TestDao.class.getDeclaredMethod("other", int.class);
            assertFalse(ctx.hasEmbeddedSqlProcedure(m));
        }
    }

    @Nested
    class ModelMetadataContextTest {

        private final ModelMetadataContext ctx = new ModelMetadataContext();

        @Test
        void registerAndRetrieve() {
            Map<Field, Annotation> fields = new HashMap<>();
            ModelMetadata metadata = new ModelMetadata(null, fields);
            ctx.registerModel(String.class, metadata);
            assertSame(metadata, ctx.getMetadataByType(String.class));
        }

        @Test
        void unregisteredType_throwsClearError() {
            assertThrows(io.github.serfom256.exceptions.entity.ModelValidationException.class,
                    () -> ctx.getMetadataByType(Integer.class));
        }

        @Test
        void isRegistered_true() {
            ctx.registerModel(String.class, new ModelMetadata(null, Map.of()));
            assertTrue(ctx.isRegistered(String.class));
        }

        @Test
        void isRegistered_false() {
            assertFalse(ctx.isRegistered(Double.class));
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class OpenMapperSQLContextTest {

        @Mock DaoMethodFacade daoMethodFacade;

        @Test
        void updateAndGetProcedure() {
            OpenMapperSQLContext ctx = new OpenMapperSQLContext(daoMethodFacade);
            SQLRecord record = new SQLRecord(List.of(), Map.of());
            ctx.updateContext("myQuery", record);


            interface TestDao {
                @io.github.serfom256.annotations.DaoMethod(procedure = "myQuery")
                void myQuery();
            }
            try {
                Method m = TestDao.class.getDeclaredMethod("myQuery");
                SQLRecord result = ctx.getSqlProcedure(TestPlans.planFor(m), null);
                assertSame(record, result);
            } catch (NoSuchMethodException e) {
                fail(e);
            }
        }

        @Test
        void getProcedure_notFound_throws() {
            OpenMapperSQLContext ctx = new OpenMapperSQLContext(daoMethodFacade);

            interface TestDao { void unknownMethod(); }
            try {
                Method m = TestDao.class.getDeclaredMethod("unknownMethod");
                assertThrows(FunctionNotFoundException.class,
                        () -> ctx.getSqlProcedure(TestPlans.planFor(m), null));
            } catch (NoSuchMethodException e) {
                fail(e);
            }
        }
    }

    @Nested
    class ModelMetadataTest {

        @Test
        void getters() throws Exception {
            Field f = String.class.getDeclaredField("value");
            Map<Field, Annotation> fields = new HashMap<>();
            ModelMetadata md = new ModelMetadata(f, fields);
            assertSame(f, md.getOptimisticLockField());
            assertSame(fields, md.getAnnotatedFields());
        }

        @Test
        void nullFields() {
            ModelMetadata md = new ModelMetadata(null, null);
            assertNull(md.getOptimisticLockField());
            assertNull(md.getAnnotatedFields());
        }
    }
}
