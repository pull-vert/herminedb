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

import jdk.incubator.sql2.ConnectionProperty;
import jdk.incubator.sql2.DataSource;

import java.util.HashMap;
import java.util.Map;

public class HermineDataSourceBuilderImpl implements DataSource.Builder {

    private boolean isBuilt = false;

    /**
     * defaultConnectionProperties can be overridden by a ConnectionBuilder
     */
    private Map<ConnectionProperty, Object> defaultConnectionProperties = new HashMap<>();

    /**
     * it is an error if a ConnectionBuilder tries to override requiredConnectionProperties
     */
    private Map<ConnectionProperty, Object> requiredConnectionProperties = new HashMap<>();

    @Override
    public HermineDataSourceBuilderImpl defaultConnectionProperty(ConnectionProperty property, Object value) {
        if (isBuilt) {
            throw new IllegalStateException("properties must be added before calling build");
        }
        if (defaultConnectionProperties.containsKey(property)) {
            throw new IllegalArgumentException("cannot set a default multiple times");
        }
        if (requiredConnectionProperties.containsKey(property)) {
            throw new IllegalArgumentException("cannot set a default that is already required");
        }
        if (!property.validate(value)) {
            throw new IllegalArgumentException("TODO");
        }
        defaultConnectionProperties.put(property, value);
        return this;
    }

    @Override
    public HermineDataSourceBuilderImpl connectionProperty(ConnectionProperty property, Object value) {
        if (isBuilt) {
            throw new IllegalStateException("properties must be added before calling build");
        }
        if (defaultConnectionProperties.containsKey(property)) {
            throw new IllegalArgumentException("cannot set a required prop that has a default");
        }
        if (requiredConnectionProperties.containsKey(property)) {
            throw new IllegalArgumentException("cannot set a required prop multiple times");
        }
        if (!property.validate(value)) {
            throw new IllegalArgumentException("TODO");
        }
        requiredConnectionProperties.put(property, value);
        return this;
    }

    @Override
    public HermineDataSourceImpl build() {
        if (isBuilt) {
            throw new IllegalStateException("cannot build more than once. All objects are use-once");
        }
        isBuilt = true;
        return new HermineDataSourceImpl(defaultConnectionProperties, requiredConnectionProperties);
    }
}
