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
package org.hermine.internal.io.tcp.client;

import org.hermine.io.tcp.client.HermineTcpClient;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.channels.Selector;

final class HermineTcpClientImpl extends HermineTcpClient {

    // This reference is used to keep track of the facade HermineTcpClient
    // that was returned to the application code.
    // It makes it possible to know when the application no longer
    // holds any reference to the HermineTcpClient.
    // Unfortunately, this information is not enough to know when
    // to exit the SelectorManager thread. Because of the asynchronous
    // nature of the API, we also need to wait until all pending operations
    // have completed.
    private final WeakReference<HermineTcpClientFacade> facadeRef;

    /**
     * This is a bit tricky:
     * 1. an HermineTcpClientFacade has a final HermineTcpClientImpl field.
     * 2. an HermineTcpClientImpl has a final WeakReference<HermineTcpClientFacade> field,
     *    where the referent is the facade created for that instance.
     * 3. We cannot just create the HermineTcpClientFacade in the HermineTcpClientImpl
     *    constructor, because it would be only weakly referenced and could
     *    be GC'ed before we can return it.
     * The solution is to use an instance of SingleFacadeFactory which will
     * allow the caller of new HermineTcpClientImpl(...) to retrieve the facade
     * after the HermineTcpClientImpl has been created.
     */
    private static final class SingleFacadeFactory {
        HermineTcpClientFacade facade;

        HermineTcpClientFacade createFacade(HermineTcpClientImpl impl) {
            assert facade == null;
            return (facade = new HermineTcpClientFacade(impl));
        }
    }

    static HermineTcpClientFacade create(HermineTcpClientBuilderImpl builder) {
        var facadeFactory = new SingleFacadeFactory();
        var impl = new HermineTcpClientImpl(builder, facadeFactory);
        impl.start();
        assert facadeFactory.facade != null;
        assert impl.facadeRef.get() == facadeFactory.facade;
        return facadeFactory.facade;
    }

    private HermineTcpClientImpl(HermineTcpClientBuilderImpl builder, SingleFacadeFactory facadeFactory) {
        facadeRef = new WeakReference<>(facadeFactory.createFacade(this));

        assert facadeRef.get() != null;
    }

    private void start() {

    }

    // Main loop for this hermine db's selector
    private final static class SelectorManager extends Thread {

        private final Selector selector;
        private volatile boolean closed;
        HermineTcpClientImpl owner;

        SelectorManager(HermineTcpClientImpl ref) throws IOException {
//            super(null, null,
//                    "HttpClient-" + ref.id + "-SelectorManager",
//                    0, false);
            owner = ref;
            selector = Selector.open();
        }
    }
}
