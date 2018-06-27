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
import org.apache.logging.log4j.LogManager;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collector;

abstract class AbstractOperationGroup<S, T> extends AbstractOperation<T> implements OperationGroup<S, T> {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(AbstractOperationGroup.class);

    static final Collector DEFAULT_COLLECTOR = Collector.of(
            () -> null,
            (a, v) -> {},
            (a, b) -> null,
            a -> null);

    static final Mono<Boolean> DEFAULT_CONDITION = Mono.just(true);

    private boolean isIndependent;
    private boolean isParallel;
    Mono<Boolean> condition;
    Collector collector;

    SubmissionImpl<T> submission = null;
    Object accumulator;

    /**
     * The last Mono of any submitted member Operation. Mutable until
     * not isHeld().
     */
    private Mono<S> memberTail;

    AbstractOperationGroup(ConnectionImpl conn, AbstractOperationGroup<? super T, ?> group) {
        this(conn, group, false, false, DEFAULT_CONDITION, DEFAULT_COLLECTOR);
    }

    AbstractOperationGroup(ConnectionImpl conn, AbstractOperationGroup<? super T, ?> group, boolean isIndependent, boolean isParallel, Mono<Boolean> condition, Collector collector) {
        super(conn, group);
        this.isIndependent = isIndependent;
        this.isParallel = isParallel;
        this.condition = condition;
        this.collector = collector;
    }

    @Override
    public AbstractOperationGroup<S, T> parallel() {
        if ( isImmutable() || isParallel) throw new IllegalStateException("TODO");
        isParallel = true;
        return this; // todo return ParallelOperationGroup
    }

    @Override
    public AbstractOperationGroup<S, T> independent() {
        if ( isImmutable() || isIndependent) throw new IllegalStateException("TODO");
        isIndependent = true;
        return this;
    }

    @Override
    public AbstractOperationGroup<S, T> conditional(CompletionStage<Boolean> condition) {
        if ( isImmutable() || condition != null) throw new IllegalStateException("TODO");
        this.condition = Mono.fromCompletionStage(condition);
        return this;
    }

    @Override
    public AbstractOperationGroup<S, T> collect(Collector<S, ?, T> c) {
        if ( isImmutable() || collector != DEFAULT_COLLECTOR) throw new IllegalStateException("TODO");
        if (c == null) throw new IllegalArgumentException("TODO");
        collector = c;
        return this;
    }

    @Override
    public SubmissionImpl<T> submitHoldingForMoreMembers() {
        if ( isImmutable() || ! isHeld() ) throw new IllegalStateException("TODO");  //TODO prevent multiple calls
        accumulator = collector.supplier().get();
        submission = super.submit();
        return submission;
    }

    @Override
    public AbstractOperation<S> catchOperation() {
        if (! isHeld() ) throw new IllegalStateException("TODO");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <R extends S> ArrayRowCountOperation<R> arrayRowCountOperation(String sql) {
        if ( ! isHeld() ) throw new IllegalStateException("TODO");
        if (sql == null) throw new IllegalArgumentException("TODO");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <R extends S> ParameterizedRowCountOperation<R> rowCountOperation(String sql) {
        if ( ! isHeld() ) throw new IllegalStateException("TODO");
        if (sql == null) throw new IllegalArgumentException("TODO");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AbstractOperation<S> operation(String sql) {
        if ( ! isHeld() ) throw new IllegalStateException("TODO");
        if (sql == null) throw new IllegalArgumentException("TODO");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <R extends S> OutOperation<R> outOperation(String sql) {
        if ( ! isHeld() ) throw new IllegalStateException("TODO");
        if (sql == null) throw new IllegalArgumentException("TODO");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <R extends S> ParameterizedRowOperation<R> rowOperation(String sql) {
        if ( ! isHeld() ) throw new IllegalStateException("TODO");
        if (sql == null) throw new IllegalArgumentException("TODO");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <R extends S> ParameterizedRowPublisherOperation<R> rowPublisherOperation(String sql) {
        if ( ! isHeld() ) throw new IllegalStateException("TODO");
        if (sql == null) throw new IllegalArgumentException("TODO");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <R extends S> MultiOperation<R> multiOperation(String sql) {
        if ( ! isHeld() ) throw new IllegalStateException("TODO");
        if (sql == null) throw new IllegalArgumentException("TODO");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AbstractOperation<TransactionOutcome> endTransactionOperation(Transaction trans) {
        if ( ! isHeld() ) throw new IllegalStateException("TODO");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <R extends S> LocalOperation<R> localOperation() {
        if ( ! isHeld() ) throw new IllegalStateException("TODO");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public OperationGroup<S, T> logger(Logger logger) {
        throw new UnsupportedOperationException("Will use other Logger");
    }

    // Override all Operation methods

    @Override
    public AbstractOperationGroup<S, T> timeout(Duration minTime) {
        super.timeout(minTime);
        return this;
    }

    @Override
    public AbstractOperationGroup<S, T> onError(Consumer<Throwable> handler) {
        super.onError(handler);
        return this;
    }

    // Internal methods

    SubmissionImpl<S> submit(AbstractOperation<S> op) {
        LOGGER.info("AbstractOperationGroup#submit");
        memberTail = op.attachErrorHandler(op.follows(memberTail, getScheduler()));
        return new SubmissionImpl(this::cancel, memberTail);
    }

    abstract protected boolean isHeld();
}
