/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import jdk.incubator.sql2.Connection
import jdk.incubator.sql2.ConnectionProperty
import mu.KotlinLogging

// Place definition above class declaration to make field static
private val logger = KotlinLogging.logger {}

internal class ConnectionBuilderImpl(
        val dataSource: DataSourceImpl,
        defaultConnectionProperties: Map<ConnectionProperty, Any>,
        specifiedConnectionProperties: Map<ConnectionProperty, Any>
) : Connection.Builder {

    private var isBuilt = false
    private val defaultProperties: MutableMap<ConnectionProperty, Any>
    private val requiredProperties: MutableMap<ConnectionProperty, Any>

    init {
        defaultProperties = HashMap(defaultConnectionProperties)
        requiredProperties = HashMap(specifiedConnectionProperties)
    }

    override fun property(property: ConnectionProperty, value: Any): ConnectionBuilderImpl {
        if (isBuilt) {
            throw IllegalStateException("The Connection was already buit")
        }
        if (requiredProperties.containsKey(property)) {
            throw IllegalArgumentException("cannot override required properties")
        }
        if (!property.validate(value)) {
            throw IllegalArgumentException("Property is not valid")
        }
        requiredProperties[property] = value
        return this
    }

    override fun build(): Connection {
        if (isBuilt) throw IllegalStateException("The Connection was already buit")
        isBuilt = true
        // replace default values with specified values where provided
        // otherwise use defaults
        defaultProperties.putAll(requiredProperties)
        logger.debug{"Creating ConnectionImpl"}
        return ConnectionImpl(dataSource, defaultProperties)
    }
}
