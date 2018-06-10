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

import java.util.Map;

class HermineConnectionImpl extends HermineOperationGroupImpl<Object, Object> implements Connection {

    private final DataSource dataSource;
    private final Map<ConnectionProperty, Object> properties;

    HermineConnectionImpl(HermineDataSourceImpl ds, Map<ConnectionProperty,Object> properties) {
        super(null, null); // hack as _this_ not allowed. See SimpleOperation constructor
        dataSource = ds;
        this.properties = properties;
    }

    @Override
    public Operation<Void> connectOperation() {
        return null;
    }

    @Override
    public Operation<Void> validationOperation(Validation depth) {
        return null;
    }

    @Override
    public Operation<Void> closeOperation() {
        return null;
    }

    @Override
    public <S, T> OperationGroup<S, T> operationGroup() {
        return null;
    }

    @Override
    public Transaction transaction() {
        return null;
    }

    @Override
    public HermineConnectionImpl registerLifecycleListener(ConnectionLifecycleListener listener) {
        return this;
    }

    @Override
    public HermineConnectionImpl deregisterLifecycleListener(ConnectionLifecycleListener listener) {
        return this;
    }

    @Override
    public Lifecycle getConnectionLifecycle() {
        return null;
    }

    @Override
    public HermineConnectionImpl abort() {
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
    public HermineConnectionImpl activate() {
        return this;
    }

    @Override
    public HermineConnectionImpl deactivate() {
        return this;
    }
}
