package io.github.serfom256.benchmark;

import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

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
