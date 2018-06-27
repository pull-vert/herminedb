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

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @param <T>
 */
class SimpleOperation<T> extends AbstractOperation<T> implements Supplier<T> {

    static <S> SimpleOperation<S> newOperation(ConnectionImpl conn,
                                               AbstractOperationGroup<? super S, ?> group,
                                               Function<SimpleOperation<S>, S> act) {
        return new SimpleOperation<>(conn, group, act);
    }

    private final Function<SimpleOperation<T>, T> action;

    protected SimpleOperation(ConnectionImpl conn,
                              AbstractOperationGroup<? super T, ?> operationGroup,
                              Function<SimpleOperation<T>, T> act) {
        super(conn, operationGroup);
        action = act;
    }

    @Override
    Mono<T> follows(Mono<?> predecessor, Scheduler scheduler) {
        return ((Mono<T>) predecessor).publishOn(scheduler);
    }

    /**
     * Computes the value of this Operation by calling the action. If this
     * Operation has been canceled throws SqlSkippedException. If the action
     * throws a checked exception, wrap that checked exception in a SqlException.
     * SqlException is unchecked as required by Supplier, and can be handled by
     * CompletionStage.
     */
    @Override
    public T get() {
        checkCanceled();
        try {
            return action.apply(this);
        }
        finally {
            operationLifecycle = OperationLifecycle.COMPLETED;
        }
    }

}