/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol. Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.io.common

import java.nio.ByteBuffer

/**
 *  This interface allows to recycle buffers used for SSL decryption.
 *  Buffers that are used for reading SSL encrypted data are typically
 *  very short lived, as it is necessary to aggregate their content
 *  before calling SSLEngine::unwrap.
 *  Because both reading and copying happen in the SelectorManager
 *  thread, then it makes it possible to pool these buffers and
 *  recycle them for the next socket read, instead of simply
 *  letting them be GC'ed. That also makes it possible to use
 *  direct byte buffers, and avoid another layer of copying by
 *  the SocketChannel implementation.
 *
 *  The HttpClientImpl has an implementation of this interface
 *  that allows to reuse the same 3 direct buffers for reading
 *  off SSL encrypted data from the socket.
 *  The BufferSupplier::get method is called by SocketTube
 *  (see SocketTube.SSLDirectBufferSource) and BufferSupplier::recycle
 *  is called by SSLFlowDelegate.Reader.
 **/
interface BufferSupplier : () -> ByteBuffer {
    /**
     * Returns a buffer to read encrypted data off the socket.
     * @return a buffer to read encrypted data off the socket.
     */
    override fun invoke(): ByteBuffer

    /**
     * Returns a buffer to the pool.
     *
     * @param buffer This must be a buffer previously obtained
     * by calling BufferSupplier::get. The caller must
     * not touch the buffer after returning it to
     * the pool.
     */
    fun recycle(buffer: ByteBuffer)
}
