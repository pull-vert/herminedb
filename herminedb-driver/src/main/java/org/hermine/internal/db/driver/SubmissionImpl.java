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
package org.hermine.internal.db.driver;

import jdk.incubator.sql2.Submission;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public class SubmissionImpl<T> implements Submission<T> {

    private final Supplier<Boolean> cancel;
    private final Mono<T> mono;
    private CompletionStage<T> publicStage;

    SubmissionImpl(Supplier<Boolean> can, Mono<T> mono) {
        cancel = can;
        this.mono = mono;
    }

    @Override
    public CompletionStage<Boolean> cancel() {
        return new CompletableFuture().completeAsync(cancel);
    }

    @Override
    public CompletionStage<T> getCompletionStage() {
        if (publicStage == null) publicStage = (mono.toFuture()).minimalCompletionStage();
        return publicStage;
    }
}
