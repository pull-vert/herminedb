/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import jdk.incubator.sql2.Session
import jdk.incubator.sql2.SessionProperty
import jdk.incubator.sql2.DataSource

/**
* Bare bones DataSource. No support for Session caching.
*/
internal class DataSourceImpl(
        private val defaultSessionProperties: Map<SessionProperty, Any>,
        private val requiredSessionProperties: Map<SessionProperty, Any>
) : DataSource {

    private val openSessions = hashSetOf<Session>()

    override fun builder(): Session.Builder =
            SessionBuilderImpl(this, defaultSessionProperties, requiredSessionProperties)

    override fun close() = openSessions.forEach(Session::close)

    fun registerSession(c: Session): DataSourceImpl {
        openSessions.add(c)
        return this
    }

    fun deregisterSession(c: Session): DataSourceImpl {
        openSessions.remove(c)
        return this
    }
}
