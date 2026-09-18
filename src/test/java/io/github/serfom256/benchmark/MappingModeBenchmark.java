package io.github.serfom256.benchmark;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import io.github.serfom256.benchmark.model.om.ProductOM;
import io.github.serfom256.benchmark.model.rec.ProductRec;
import io.github.serfom256.core.query.mapping.MappingPlanRegistry;
import io.github.serfom256.core.query.mapping.ResultSetGraphReader;

/**
 * Row mapping with the database taken out: setter-based population of a mutable
 * class against canonical-constructor population of a record, over the same
 * five-column row. The rest of the suite is dominated by the JDBC round trip,
 * which hides a difference this size.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 3)
@Fork(0)
public class MappingModeBenchmark {

    @State(Scope.Thread)
    public static class MapperState {
        ResultSetGraphReader reader;
        FakeResultSet.Cursor cursor;

        @Setup
        public void setup() {
            reader = new ResultSetGraphReader(new MappingPlanRegistry(null));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", 1L);
            row.put("name", "widget");
            row.put("price", 1.5d);
            row.put("quantity", 3);
            row.put("description", "desc");
            cursor = FakeResultSet.singleRow(row);
        }
    }

    @Benchmark
    public Object setters_mutableClass(MapperState state) throws SQLException {
        return state.reader.map(ProductOM.class, false, state.cursor.rewind());
    }

    @Benchmark
    public Object canonicalConstructor_record(MapperState state) throws SQLException {
        return state.reader.map(ProductRec.class, false, state.cursor.rewind());
    }
}
