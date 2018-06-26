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

import jdk.incubator.sql2.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collector;

class OperationGroupImpl<S, T> extends AbstractOperation<T> implements OperationGroup<S, T> {

    static final Collector DEFAULT_COLLECTOR = Collector.of(
            () -> null,
            (a, v) -> {},
            (a, b) -> null,
            a -> null);

    static final Mono<Boolean> DEFAULT_CONDITION = Mono.just(true);

    private Mono<Boolean> condition = DEFAULT_CONDITION;

    private Object accumulator;
    private Collector collector;

    /**
     * completed when this OperationGroup is no longer held. Completion of this
     * OperationGroup depends on held.
     *
     * @see submit, releaseProhibitingMoreOperations, submitHoldingForMoreOperations
     */
    private final Mono<S> held;

    /**
     * predecessor of all member Operations and the OperationGroup itself
     */
    private final Mono head;

    /**
     * The last Mono of any submitted member Operation. Mutable until
     * not isHeld().
     */
    private Mono<S> memberTail;

    protected OperationGroupImpl(ConnectionImpl conn, OperationGroupImpl<? super T, ?> group) {
        super(conn, group);
        held = Mono.empty();
        head = Mono.empty();
        memberTail = head;
        collector = DEFAULT_COLLECTOR;
    }

    @Override
    public OperationGroupImpl<S, T> parallel() {
        return this;
    }

    @Override
    public OperationGroupImpl<S, T> independent() {
        return this;
    }

    @Override
    public OperationGroupImpl<S, T> conditional(CompletionStage<Boolean> condition) {
        return this;
    }

    @Override
    public Submission<T> submitHoldingForMoreMembers() {
        return null;
    }

    @Override
    public Submission<T> releaseProhibitingMoreMembers() {
        return null;
    }

    @Override
    public OperationGroupImpl<S, T> collect(Collector<S, ?, T> c) {
        return this;
    }

    @Override
    public Operation<S> catchOperation() {
        return null;
    }

    @Override
    public OperationGroup<S, T> catchErrors() {
        return null;
    }

    @Override
    public <R extends S> ArrayRowCountOperation<R> arrayRowCountOperation(String sql) {
        return null;
    }

    @Override
    public <R extends S> ParameterizedRowCountOperation<R> rowCountOperation(String sql) {
        return null;
    }

    @Override
    public Operation<S> operation(String sql) {
        return null;
    }

    @Override
    public <R extends S> OutOperation<R> outOperation(String sql) {
        return null;
    }

    @Override
    public <R extends S> ParameterizedRowOperation<R> rowOperation(String sql) {
        return null;
    }

    @Override
    public <R extends S> ParameterizedRowPublisherOperation<R> rowPublisherOperation(String sql) {
        return null;
    }

    @Override
    public <R extends S> MultiOperation<R> multiOperation(String s) {
        return null;
    }

    @Override
    public Operation<TransactionOutcome> endTransactionOperation(Transaction trans) {
        return null;
    }

    @Override
    public CompletionStage<TransactionOutcome> commitMaybeRollback(Transaction trans) {
        return null;
    }

    @Override
    public <R extends S> LocalOperation<R> localOperation() {
        return null;
    }

    @Override
    public OperationGroup<S, T> logger(Logger logger) {
        throw new UnsupportedOperationException("Will use other Logger");
    }

    // Override all Operation methods

    @Override
    public OperationGroupImpl<S, T> onError(Consumer<Throwable> handler) {
        return this;
    }

    @Override
    public OperationGroupImpl<S, T> timeout(Duration minTime) {
        return this;
    }

    @Override
    public SubmissionImpl<T> submit() {
        if ( isImmutable() ) throw new IllegalStateException("TODO");
        accumulator = collector.supplier().get();
        SubmissionImpl<T> sub = super.submit();
        held.subscribe();
        return sub;
    }

    // Internal methods

    SubmissionImpl<S> submit(AbstractOperation<S> op) {
        memberTail = op.attachErrorHandler(op.follows(memberTail, getScheduler()));
        return new SubmissionImpl(this::cancel, memberTail);
    }

    @Override
    Mono<T> follows(Mono<?> predecessor, Scheduler scheduler) {
        return condition.flatMap(cond -> {
                    if (cond) {
                        head.thenReturn(predecessor);
                        return held.then(memberTail.map(t -> (T)collector.finisher()
                                        .apply(accumulator))
                                .publishOn(scheduler)
                        );
                    }
                    else {
                        return Mono.empty();
                    }
                }
        );
    }
}
