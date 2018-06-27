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
import reactor.core.scheduler.Scheduler;

import java.util.Map;
import java.util.function.Consumer;

class ConnectionImpl extends SequentialOperationGroup<Object, Object> implements Connection {

    // FIELDS
    private final DataSource dataSource;
    private final Map<ConnectionProperty, Object> properties;

    private final Scheduler scheduler;

    ConnectionImpl(DataSourceImpl ds, Map<ConnectionProperty, Object> properties) {
        super(null, null); // hack as _this_ not allowed. See SimpleOperation constructor
        dataSource = ds;
        this.properties = properties;
        ConnectionProperty execProp = HermineConnectionProperty.SCHEDULER;
        scheduler = (Scheduler) properties.getOrDefault(execProp, execProp.defaultValue());
    }

    @Override
    public Operation<Void> connectOperation() {
        if (!isHeld()) {
            throw new IllegalStateException("TODO");
        }
        return SimpleOperation.<Void>newOperation(this, this, this::hermineConnect);
    }

    @Override
    public Operation<Void> validationOperation(Validation depth) {
        return null;
    }

    @Override
    public Operation<Void> closeOperation() {
        return UnskippableOperation.<Void>newOperation(this, this, this::hermineClose);
    }

    @Override
    public <S, T> AbstractOperationGroup<S, T> operationGroup() {
        return null;
    }

    @Override
    public Transaction transaction() {
        return null;
    }

    @Override
    public ConnectionImpl registerLifecycleListener(ConnectionLifecycleListener listener) {
        return this;
    }

    @Override
    public ConnectionImpl deregisterLifecycleListener(ConnectionLifecycleListener listener) {
        return this;
    }

    @Override
    public Lifecycle getConnectionLifecycle() {
        return null;
    }

    @Override
    public ConnectionImpl abort() {
        return this;
    }

    @Override
    public Map<ConnectionProperty, Object> getProperties() {
        return null;
    }

    @Override
    public ShardingKey.Builder shardingKeyBuilder() {
        return null;
    }

    @Override
    public ConnectionImpl requestHook(Consumer<Long> request) {
        return null;
    }

    @Override
    public ConnectionImpl activate() {
        return this;
    }

    @Override
    public ConnectionImpl deactivate() {
        return this;
    }

    // INTERNAL

    @Override
    protected Scheduler getScheduler() {
        return scheduler;
    }

    private Void hermineConnect(AbstractOperation<Void> op) {
        return null;
    }

    private Void hermineClose(AbstractOperation<Void> op) {
        return null;
    }
}
