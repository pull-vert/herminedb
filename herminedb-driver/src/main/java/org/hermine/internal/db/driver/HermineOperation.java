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

import jdk.incubator.sql2.Operation;
import jdk.incubator.sql2.Submission;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * TODO : not based on Completion Stage but on close of previous Flow Publisher
 */
abstract class HermineOperation<T> implements Operation<T> {

    // attributes
    private Duration timeout = null;
    private Consumer<Throwable> errorHandler = null;

    // internal state
    private final HermineConnectionImpl connection;
    private final HermineOperationGroupImpl<T, ?> group;

    HermineOperation(HermineConnectionImpl conn, HermineOperationGroupImpl operationGroup) {
        // passing null for connection and operationGroup is a hack. It is not
        // possible to pass _this_ to a super constructor so we define null to mean
        // _this_. Yuck. Only used by Connection.
        connection = conn == null ? (HermineConnectionImpl) this : conn;
        group = operationGroup == null ? (HermineOperationGroupImpl) this : operationGroup;
    }

    @Override
    public HermineOperation<T> onError(Consumer<Throwable> handler) {
        return this;
    }

    @Override
    public HermineOperation<T> timeout(Duration minTime) {
        return this;
    }

    @Override
    public Submission<T> submit() {
        return null;
    }

    Executor getExecutor() {
        return connection.getExecutor();
    }
}
