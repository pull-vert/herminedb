/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol. Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.io.tcp.client

import org.hermine.io.tcp.client.HermineTcpClient

internal class TcpClientBuilderImpl : HermineTcpClient.Builder {
    internal var port = 35700

    override fun port(port: Int): TcpClientBuilderImpl {
        if (port < 1) {
            throw IllegalArgumentException("int must be greater or equals than 1")
        }
        this.port = port
        return this
    }

    override fun build(): HermineTcpClient = createTcpClientImpl(this)
}
