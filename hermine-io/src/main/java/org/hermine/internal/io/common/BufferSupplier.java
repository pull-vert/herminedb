/*
 * Copyright 2018 HermineTcpClient's author : Frédéric Montariol,
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
package org.hermine.internal.io.common;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

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
public interface BufferSupplier extends Supplier<ByteBuffer> {
    /**
     * Returns a buffer to read encrypted data off the socket.
     * @return a buffer to read encrypted data off the socket.
     */
    ByteBuffer get();

    /**
     * Returns a buffer to the pool.
     *
     * @param buffer This must be a buffer previously obtained
     *               by calling BufferSupplier::get. The caller must
     *               not touch the buffer after returning it to
     *               the pool.
     */
    void recycle(ByteBuffer buffer);
}

