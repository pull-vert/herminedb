/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import jdk.incubator.sql2.Connection
import jdk.incubator.sql2.ConnectionProperty
import jdk.incubator.sql2.DataSource

/**
* Bare bones DataSource. No support for Connection caching.
*/
internal class DataSourceImpl(
        private val defaultConnectionProperties: Map<ConnectionProperty, Any>,
        private val requiredConnectionProperties: Map<ConnectionProperty, Any>
) : DataSource {

    private val openConnections = hashSetOf<Connection>()

    override fun builder(): Connection.Builder =
            ConnectionBuilderImpl(this, defaultConnectionProperties, requiredConnectionProperties)

    override fun close() = openConnections.forEach(Connection::close)

    fun registerConnection(c: Connection): DataSourceImpl {
        openConnections.add(c)
        return this
    }

    fun deregisterConnection(c: Connection): DataSourceImpl {
        openConnections.remove(c)
        return this
    }
}
