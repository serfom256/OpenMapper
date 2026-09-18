package io.github.serfom256.unit.model;

import io.github.serfom256.core.query.model.BoundSql;
import io.github.serfom256.core.query.model.ModelSpecifications;
import io.github.serfom256.core.query.model.QueryParameters;
import io.github.serfom256.core.query.model.QuerySpecifications;
import io.github.serfom256.model.operations.DmlOperation;
import io.github.serfom256.parser.model.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataClassesTest {

    @Nested
    class SQLTokenTest {
        @Test
        void constructorAndGetters() {
            SQLToken token = new SQLToken("SELECT", 0);
            assertEquals("SELECT", token.getData());
            assertEquals(0, token.getPosition());
        }

        @Test
        void toStringReturnsData() {
            SQLToken token = new SQLToken("hello", 1);
            assertEquals("hello", token.toString());
        }
    }

    @Nested
    class SQLRecordTest {
        @Test
        void constructorAndGetters() {
            SQLToken t = new SQLToken("data", 0);
            Map<String, SQLToken> vars = Map.of("id", t);
            SQLRecord record = new SQLRecord(List.of(t), vars);
            assertEquals(1, record.getSql().size());
            assertEquals(1, record.getVariables().size());
        }

        @Test
        void sqlListIsUnmodifiable() {
            SQLRecord record = new SQLRecord(List.of(new SQLToken("x", 0)), Map.of());
            assertThrows(UnsupportedOperationException.class, () -> record.getSql().add(new SQLToken("y", 1)));
        }

        @Test
        void toStringContainsData() {
            SQLRecord record = new SQLRecord(List.of(), Map.of());
            assertNotNull(record.toString());
            assertTrue(record.toString().contains("SqlRecord"));
        }
    }

    @Nested
    class SQLProcedureTest {
        @Test
        void constructorAndGetters() {
            SQLProcedure proc = new SQLProcedure("myProc", "SELECT 1");
            assertEquals("myProc", proc.getFunctionName());
            assertEquals("SELECT 1", proc.getFunctionBody());
        }
    }

    @Nested
    class CharLocationTest {
        @Test
        void constructorAndGetters() {
            CharLocation loc = new CharLocation(1, 5, 10);
            assertEquals(1, loc.getLine());
            assertEquals(5, loc.getColumn());
            assertEquals(10, loc.getOffset());
        }
    }

    @Nested
    class SpanLocationTest {
        @Test
        void constructorAndGetters() {
            CharLocation start = new CharLocation(1, 0, 0);
            CharLocation end = new CharLocation(1, 5, 5);
            SpanLocation span = new SpanLocation(start, end);
            assertSame(start, span.getStart());
            assertSame(end, span.getEnd());
        }
    }

    @Nested
    class QueryParametersTest {
        @Test
        void constructorAndGetters() {
            QuerySpecifications specs = new QuerySpecifications();
            SQLRecord record = new SQLRecord(List.of(), Map.of());
            BoundSql boundSql = new BoundSql("SELECT 1", List.of());
            QueryParameters params = new QueryParameters(boundSql, specs, record);
            assertSame(boundSql, params.getBoundSql());
            assertSame(specs, params.getQuerySpecifications());
            assertSame(record, params.getSource());
        }

        @Test
        void setters() {
            QueryParameters params = new QueryParameters(null, null, null);
            params.setBoundSql(new BoundSql("q", List.of()));
            params.setQuerySpecifications(new QuerySpecifications());
            params.setSource(new SQLRecord(List.of(), Map.of()));
            assertEquals("q", params.getBoundSql().getSql());
            assertNotNull(params.getQuerySpecifications());
            assertNotNull(params.getSource());
        }
    }

    @Nested
    class QuerySpecificationsTest {
        @Test
        void allGettersAndSetters() {
            QuerySpecifications qs = new QuerySpecifications();
            qs.setOperation(DmlOperation.SELECT);
            qs.setReturnType(String.class);
            qs.setShouldReturnGeneratedKeys(true);
            qs.setHasOptimisticLock(false);
            qs.setParams(Map.of("a", 1));

            assertEquals(DmlOperation.SELECT, qs.getOperation());
            assertEquals(String.class, qs.getReturnType());
            assertTrue(qs.shouldReturnGeneratedKeys());
            assertFalse(qs.hasOptimisticLock());
            assertEquals(1, qs.getParams().get("a"));
        }

        @Test
        void dataSourceDefaultsToNull() {
            QuerySpecifications qs = new QuerySpecifications();
            assertNull(qs.getDataSource());
        }
    }

    @Nested
    class ModelSpecificationsTest {
        @Test
        void constructorWithParams() {
            ModelSpecifications ms = new ModelSpecifications(Map.of("a", 1));
            assertEquals(1, ms.getParams().get("a"));
        }

        @Test
        void defaultConstructor() {
            ModelSpecifications ms = new ModelSpecifications();
            assertNull(ms.getParams());
        }

        @Test
        void allSettersAndGetters() {
            ModelSpecifications ms = new ModelSpecifications();
            ms.setParams(Map.of("x", "y"));
            ms.setOptimisticLockName("version");
            ms.setGeneratedOptimisticLockValue(5);

            assertEquals("y", ms.getParams().get("x"));
            assertEquals("version", ms.getOptimisticLockName());
            assertEquals(5, ms.getGeneratedOptimisticLockValue());
        }
    }

    @Nested
    class DmlOperationTest {
        @Test
        void allValuesExist() {
            assertEquals(5, DmlOperation.values().length);
            assertNotNull(DmlOperation.valueOf("SELECT"));
            assertNotNull(DmlOperation.valueOf("INSERT"));
            assertNotNull(DmlOperation.valueOf("UPDATE"));
            assertNotNull(DmlOperation.valueOf("DELETE"));
            assertNotNull(DmlOperation.valueOf("MERGE"));
        }
    }
}
