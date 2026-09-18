package io.github.serfom256.benchmark;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Entry point for ORM benchmarks.
 *
 * Run with:
 *   mvn test-compile exec:java \
 *       -Dexec.mainClass="io.github.serfom256.benchmark.BenchmarkRunner" \
 *       -Dexec.classpathScope=test
 *
 * The first argument is a JMH include regex matched against the fully-qualified
 * benchmark method name. Examples:
 *   ... -Dexec.args="OrmBenchmark"            (per-operation latency suite)
 *   ... -Dexec.args="ReadHeavyBenchmark"      (read-heavy throughput suite, mirrors OpenMapperTest)
 *   ... -Dexec.args="OrmBenchmark.*select"    (only SELECT benchmarks)
 *   ... -Dexec.args="findById_OpenMapperCached" (a single benchmark)
 * With no argument, the per-operation OrmBenchmark suite runs.
 *
 * Prerequisites:
 *   - Docker must be running (Testcontainers starts PostgreSQL)
 *   - Jimmer APT must have processed test sources (mvn test-compile)
 */
public class BenchmarkRunner {

    public static void main(String[] args) throws RunnerException {
        String include = (args.length > 0 && !args[0].isBlank())
                ? args[0]
                : OrmBenchmark.class.getSimpleName();

        Options opt = new OptionsBuilder()
                .include(include)
                .resultFormat(ResultFormatType.TEXT)
                .build();

        new Runner(opt).run();
    }
}
