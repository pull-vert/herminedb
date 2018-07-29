/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol. Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import jdk.incubator.sql2.Session
import jdk.incubator.sql2.SessionProperty
import mu.KotlinLogging

// Place definition above class declaration to make field static
private val logger = KotlinLogging.logger {}

internal class SessionBuilderImpl(
        val dataSource: DataSourceImpl,
        defaultSessionProperties: Map<SessionProperty, Any>,
        specifiedSessionProperties: Map<SessionProperty, Any>
) : Session.Builder {

    private var isBuilt = false
    private val defaultProperties: MutableMap<SessionProperty, Any>
    private val requiredProperties: MutableMap<SessionProperty, Any>

    init {
        defaultProperties = HashMap(defaultSessionProperties)
        requiredProperties = HashMap(specifiedSessionProperties)
    }

    override fun property(property: SessionProperty, value: Any): SessionBuilderImpl {
        if (isBuilt) {
            throw IllegalStateException("The Session was already buit")
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

    override fun build(): Session {
        if (isBuilt) throw IllegalStateException("The Session was already buit")
        isBuilt = true
        // replace default values with specified values where provided
        // otherwise use defaults
        defaultProperties.putAll(requiredProperties)
        logger.debug{"Creating SessionImpl"}
        return SessionImpl(dataSource, defaultProperties)
    }
}
