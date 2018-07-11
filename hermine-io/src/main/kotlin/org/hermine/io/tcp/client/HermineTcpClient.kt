/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.io.tcp.client

import org.hermine.internal.io.tcp.client.TcpClientBuilderImpl

/**
 * Returns a new {@code HermineTcpClient} with default settings.
 *
 * <p> Equivalent to {@code newBuilder().build()}.
 *
 * <p> The default settings include: the "GET" request method, a preference
 * of {@linkplain SSLContext#getDefault() default SSL context}.
 *
 * @implNote The system-wide default values are retrieved at the time the
 * {@code HermineTcpClient} instance is constructed. Changing the system-wide
 * values after an {@code HermineTcpClient} instance has been built, for
 * instance, by calling {@link SSLContext#setDefault(SSLContext)}, has no effect on already
 * built instances.
 *
 * @return a new HermineTcpClient
 */
public fun newHermineTcpClient() = newBuilder().build()

/**
 * Creates a new {@code HermineTcpClient} builder.
 *
 * @return an {@code HermineTcpClient.Builder}
 */
fun newBuilder(): HermineTcpClient.Builder = TcpClientBuilderImpl()

abstract class HermineTcpClient protected constructor() {
    /**
     * A builder of {@linkplain HermineTcpClient Hermine DB}.
     *
     * <p> Builders are created by invoking {@link HermineTcpClient#newBuilder()
     * newBuilder}. Each of the setter methods modifies the state of the builder
     * and returns the same instance. Builders are not thread-safe and should not be
     * used concurrently from multiple threads without external synchronization.
     */
    interface Builder {
        /**
         * Sets the TCP port this HermineTcpClient will connect to
         *
         * @param port HermineTcpClient TCP port
         * @return this builder
         * @throws IllegalArgumentException if the given priority is out of range
         */
        fun port(port: Int): Builder

        /**
         * Returns a new [HermineTcpClient] built from the current state of this
         * builder.
         *
         * @return a new `HermineTcpClient`
         */
        fun build(): HermineTcpClient
    }
}
