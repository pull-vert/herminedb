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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.function.Function;

/**
 *
 */
class UnskippableOperation<T> extends SimpleOperation<T> {

    private static final Logger LOGGER = LogManager.getLogger(UnskippableOperation.class);

    static <S> UnskippableOperation<S> newOperation(ConnectionImpl conn,
                                                    AbstractOperationGroup<? super S, ?> group,
                                                    Function<SimpleOperation<S>, S> action) {
        return new UnskippableOperation<>(conn, group, action);
    }

    protected UnskippableOperation(ConnectionImpl conn,
                                   AbstractOperationGroup<? super T, ?> operationGroup,
                                   Function<SimpleOperation<T>, T> action) {
        super(conn, operationGroup, action);
    }

    @Override
    Mono<T> follows(Mono<?> predecessor, Scheduler scheduler) {
        LOGGER.info("UnskippableOperation#follows");
        return ((Mono<T>) predecessor)
                .onErrorMap(t -> {
                    if (errorHandler != null) errorHandler.accept(t);
                    return t;
                })
                .publishOn(scheduler);
    }
}
