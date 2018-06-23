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

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.ConnectionProperty;
import jdk.incubator.sql2.DataSource;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Bare bones DataSource. No support for Connection caching.
 */
class DataSourceImpl implements DataSource {

    private final Map<ConnectionProperty, Object> defaultConnectionProperties;
    private final Map<ConnectionProperty, Object> requiredConnectionProperties;

    private final Set<Connection> openConnections = new HashSet<>();

    DataSourceImpl(Map<ConnectionProperty, Object> defaultProps,
                   Map<ConnectionProperty, Object> requiredProps) {
        super();
        defaultConnectionProperties = defaultProps;
        requiredConnectionProperties = requiredProps;
    }

    @Override
    public ConnectionBuilderImpl builder() {
        return new ConnectionBuilderImpl(this, defaultConnectionProperties, requiredConnectionProperties);
    }

    @Override
    public void close() {
        openConnections.forEach(Connection::close);
    }

    DataSourceImpl registerConnection(Connection c) {
        openConnections.add(c);
        return this;
    }

    DataSourceImpl deregisterConnection(Connection c) {
        openConnections.remove(c);
        return this;
    }
}
