/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.io.tcp.client

import kotlinx.atomicfu.atomic
import org.hermine.io.tcp.client.HermineTcpClient
import java.lang.ref.WeakReference
import java.nio.channels.Selector

internal class TcpClientImpl(
        builder: TcpClientBuilderImpl,
        facadeFactory: SingleFacadeFactory
) : HermineTcpClient {

    private val clientIds = atomic(0L)
    private val id: Long = clientIds.incrementAndGet()

    /**
     * This reference is used to keep track of the facade [TcpClientFacade]
     * that was returned to the application code.
     * It makes it possible to know when the application no longer
     * holds any reference to the HermineTcpClient.
     * Unfortunately, this information is not enough to know when
     * to exit the [SelectorManager] thread. Because of the asynchronous
     * nature of the API, we also need to wait until all pending operations
     * have completed.
     */
    internal val facadeRef: WeakReference<TcpClientFacade> = WeakReference(facadeFactory.createFacade(this))

    internal fun start() {

    }

    // Main loop for this selector
    private class SelectorManager(owner: TcpClientImpl) : Thread(null, null,
                    "HermineTcpClient-" + owner.id + "-SelectorManager",
                    0, false) {

        private val selector: Selector = Selector.open()
        @Volatile
        private var closed: Boolean = false
    }
}

internal fun createTcpClientImpl(builder: TcpClientBuilderImpl): TcpClientFacade {
    val facadeFactory = SingleFacadeFactory()
    val impl = TcpClientImpl(builder, facadeFactory)
    impl.start()
    assert(impl.facadeRef.get() == facadeFactory.facade)
    return facadeFactory.facade
}

/**
 * This is a bit tricky:
 * 1. an [TcpClientFacade] has a final [TcpClientImpl] field.
 * 2. an [TcpClientImpl] has a final [WeakReference]<[TcpClientFacade]> field,
 * where the referent is the facade created for that instance.
 * 3. We cannot just create the [TcpClientFacade] in the [TcpClientImpl]
 * constructor, because it would be only weakly referenced and could
 * be GC'ed before we can return it.
 * The solution is to use an instance of [SingleFacadeFactory] which will
 * allow the caller of new [TcpClientImpl](...) to retrieve the facade
 * after the [TcpClientImpl] has been created.
 */
internal class SingleFacadeFactory {
    internal lateinit var facade: TcpClientFacade

    internal fun createFacade(impl: TcpClientImpl): TcpClientFacade {
        assert(!::facade.isInitialized)
        facade = TcpClientFacade(impl)
        return facade
    }
}
