/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol. Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.io.tcp.client

import org.hermine.io.tcp.client.HermineTcpClient

/**
 * An [TcpClientFacade] is a simple class that wraps an [TcpClientImpl] implementation
 * and delegates everything to its implementation delegate.
 */
internal class TcpClientFacade(val delegate: TcpClientImpl) : HermineTcpClient by delegate {

    override fun toString(): String {
        return delegate.toString()
    }
}
