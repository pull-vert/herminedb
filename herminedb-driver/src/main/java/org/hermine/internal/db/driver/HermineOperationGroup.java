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

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collector;

class HermineOperationGroup<S, T> extends HermineOperation<T> implements OperationGroup<S, T> {

    HermineOperationGroup(HermineConnection conn, HermineOperationGroup<? super T, ?> group) {
        super(conn, group);
    }

    @Override
    public HermineOperationGroup<S, T> parallel() {
        return this;
    }

    @Override
    public HermineOperationGroup<S, T> independent() {
        return this;
    }

    @Override
    public HermineOperationGroup<S, T> conditional(CompletionStage<Boolean> condition) {
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
    public HermineOperationGroup<S, T> collect(Collector<S, ?, T> c) {
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
    public HermineOperationGroup<S, T> onError(Consumer<Throwable> handler) {
        return this;
    }

    @Override
    public HermineOperationGroup<S, T> timeout(Duration minTime) {
        return this;
    }

    @Override
    public Submission<T> submit() {
        return null;
    }
}
