/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import jdk.incubator.sql2.DataSource
import jdk.incubator.sql2.DataSourceProperty
import jdk.incubator.sql2.SessionProperty
import java.util.function.LongConsumer


internal class DataSourceBuilderImpl : DataSource.Builder {
    private var isBuilt = false

    private val dataSourceProperties = hashMapOf<DataSourceProperty, Any>()

    /**
     * defaultSessionProperties can be overridden by a SessionBuilder
     */
    private val defaultSessionProperties = hashMapOf<SessionProperty, Any>()

    /**
     * it is an error if a SessionBuilder tries to override requiredSessionProperties
     */
    private val requiredSessionProperties = hashMapOf<SessionProperty, Any>()

    override fun property(property: DataSourceProperty, value: Any): DataSourceBuilderImpl {
        if (isBuilt) {
            throw IllegalStateException("properties must be added before calling build")
        }
        if (dataSourceProperties.containsKey(property)) {
            throw IllegalArgumentException("cannot set a property multiple times")
        }
        if (!property.validate(value)) {
            throw IllegalArgumentException("TODO")
        }
        dataSourceProperties[property] = value
        return this
    }

    override fun defaultSessionProperty(property: SessionProperty, value: Any): DataSourceBuilderImpl {
        if (isBuilt) {
            throw IllegalStateException("properties must be added before calling build")
        }
        if (defaultSessionProperties.containsKey(property)) {
            throw IllegalArgumentException("cannot set a default multiple times")
        }
        if (requiredSessionProperties.containsKey(property)) {
            throw IllegalArgumentException("cannot set a default that is already required")
        }
        if (!property.validate(value)) {
            throw IllegalArgumentException("TODO")
        }
        defaultSessionProperties[property] = value
        return this
    }

    override fun sessionProperty(property: SessionProperty, value: Any): DataSourceBuilderImpl {
        if (isBuilt) {
            throw IllegalStateException("properties must be added before calling build")
        }
        if (defaultSessionProperties.containsKey(property)) {
            throw IllegalArgumentException("cannot set a required prop that has a default")
        }
        if (requiredSessionProperties.containsKey(property)) {
            throw IllegalArgumentException("cannot set a required prop multiple times")
        }
        if (!property.validate(value)) {
            throw IllegalArgumentException("TODO")
        }
        requiredSessionProperties[property] = value
        return this
    }

    override fun requestHook(request: LongConsumer): DataSourceBuilderImpl {
        return this
    }

    override fun build(): DataSourceImpl {
        if (isBuilt) {
            throw IllegalStateException("cannot build more than once. All objects are use-once")
        }
        isBuilt = true
        return DataSourceImpl(defaultSessionProperties, requiredSessionProperties)
    }
}
