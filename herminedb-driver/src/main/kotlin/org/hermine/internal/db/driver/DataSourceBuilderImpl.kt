/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import jdk.incubator.sql2.ConnectionProperty
import jdk.incubator.sql2.DataSource
import java.util.function.Consumer

internal class DataSourceBuilderImpl : DataSource.Builder {
    private var isBuilt = false

    /**
     * defaultConnectionProperties can be overridden by a ConnectionBuilder
     */
    private val defaultConnectionProperties = hashMapOf<ConnectionProperty, Any>()

    /**
     * it is an error if a ConnectionBuilder tries to override requiredConnectionProperties
     */
    private val requiredConnectionProperties = hashMapOf<ConnectionProperty, Any>()

    override fun defaultConnectionProperty(property: ConnectionProperty, value: Any): DataSourceBuilderImpl {
        if (isBuilt) {
            throw IllegalStateException("properties must be added before calling build")
        }
        if (defaultConnectionProperties.containsKey(property)) {
            throw IllegalArgumentException("cannot set a default multiple times")
        }
        if (requiredConnectionProperties.containsKey(property)) {
            throw IllegalArgumentException("cannot set a default that is already required")
        }
        if (!property.validate(value)) {
            throw IllegalArgumentException("TODO")
        }
        defaultConnectionProperties[property] = value
        return this
    }

    override fun connectionProperty(property: ConnectionProperty, value: Any): DataSourceBuilderImpl {
        if (isBuilt) {
            throw IllegalStateException("properties must be added before calling build")
        }
        if (defaultConnectionProperties.containsKey(property)) {
            throw IllegalArgumentException("cannot set a required prop that has a default")
        }
        if (requiredConnectionProperties.containsKey(property)) {
            throw IllegalArgumentException("cannot set a required prop multiple times")
        }
        if (!property.validate(value)) {
            throw IllegalArgumentException("TODO")
        }
        requiredConnectionProperties[property] = value
        return this
    }

    override fun requestHook(request: Consumer<Long>): DataSourceBuilderImpl {
        return this
    }

    override fun build(): DataSourceImpl {
        if (isBuilt) {
            throw IllegalStateException("cannot build more than once. All objects are use-once")
        }
        isBuilt = true
        return DataSourceImpl(defaultConnectionProperties, requiredConnectionProperties)
    }
}
