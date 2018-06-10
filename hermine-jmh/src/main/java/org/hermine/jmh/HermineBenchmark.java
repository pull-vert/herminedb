/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol,
 * and explicitly declared author of this file if provided.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    private static final int N = 1_000_000;

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
