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

import java.util.HashMap;
import java.util.Map;

class HermineConnectionBuilder implements Connection.Builder {

    private boolean isBuilt = false;
    private final HermineDataSource dataSource;
    private final Map<ConnectionProperty, Object> defaultProperties;
    private final Map<ConnectionProperty, Object> requiredProperties;

    HermineConnectionBuilder(HermineDataSource ds,
                             Map<ConnectionProperty, Object> defaultConnectionProperties,
                             Map<ConnectionProperty, Object> specifiedConnectionProperties) {
        super();
        dataSource = ds;
        defaultProperties = new HashMap<>(defaultConnectionProperties);
        requiredProperties = new HashMap<>(specifiedConnectionProperties);
    }

    @Override
    public HermineConnectionBuilder property(ConnectionProperty property, Object value) {
        if (isBuilt) {
            throw new IllegalStateException("TODO");
        }
        if (requiredProperties.containsKey(property)) {
            throw new IllegalArgumentException("cannot override required properties");
        }
        if (!property.validate(value)) {
            throw new IllegalArgumentException("TODO");
        }
        requiredProperties.put(property, value);
        return this;
    }

    @Override
    public Connection build() {
        if (isBuilt) {
            throw new IllegalStateException("TODO");
        }
        isBuilt = true;
        // replace default values with specified values where provided
        // otherwise use defaults
        defaultProperties.putAll(requiredProperties);
        return new HermineConnection(dataSource, defaultProperties);
    }
}
