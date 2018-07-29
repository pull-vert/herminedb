/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol. Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.jmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class HermineBenchmark {

    private static final int N = 100_000;

    public static void main(String... args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(".*")
                .forks(2)
                .warmupIterations(5)
                .measurementIterations(10)
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.MILLISECONDS)
                .resultFormat(ResultFormatType.JSON)
                .result("build/jmh-result.json")
                .shouldFailOnError(true)
                .build();

        new Runner(options).run();
    }

    @Benchmark
    public long benchSimple() {
        long result = 0;
        for (int i = 0; i < N; i++) {
            result+= i;
        }
        return result;
    }
}
