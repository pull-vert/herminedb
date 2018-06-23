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

final class TcpClientImpl extends HermineTcpClient {

    /**
     * This reference is used to keep track of the facade {@link TcpClientFacade}
     * that was returned to the application code.
     * It makes it possible to know when the application no longer
     * holds any reference to the HermineTcpClient.
     * Unfortunately, this information is not enough to know when
     * to exit the {@link SelectorManager} thread. Because of the asynchronous
     * nature of the API, we also need to wait until all pending operations
     * have completed.
     */
    private final WeakReference<TcpClientFacade> facadeRef;

    /**
     * This is a bit tricky:
     * 1. an {@link TcpClientFacade} has a final {@link TcpClientImpl} field.
     * 2. an {@link TcpClientImpl} has a final {@link WeakReference}<{@link TcpClientFacade}> field,
     * where the referent is the facade created for that instance.
     * 3. We cannot just create the {@link TcpClientFacade} in the {@link TcpClientImpl}
     * constructor, because it would be only weakly referenced and could
     * be GC'ed before we can return it.
     * The solution is to use an instance of {@link SingleFacadeFactory} which will
     * allow the caller of new {@link TcpClientImpl}(...) to retrieve the facade
     * after the {@link TcpClientImpl} has been created.
     */
    private static final class SingleFacadeFactory {
        TcpClientFacade facade;

        TcpClientFacade createFacade(TcpClientImpl impl) {
            assert facade == null;
            return (facade = new TcpClientFacade(impl));
        }
    }

    static TcpClientFacade create(TcpClientBuilderImpl builder) {
        var facadeFactory = new SingleFacadeFactory();
        var impl = new TcpClientImpl(builder, facadeFactory);
        impl.start();
        assert facadeFactory.facade != null;
        assert impl.facadeRef.get() == facadeFactory.facade;
        return facadeFactory.facade;
    }

    private TcpClientImpl(TcpClientBuilderImpl builder, SingleFacadeFactory facadeFactory) {
        facadeRef = new WeakReference<>(facadeFactory.createFacade(this));

        assert facadeRef.get() != null;
    }

    private void start() {

    }

    // Main loop for this selector
    private final static class SelectorManager extends Thread {

        private final Selector selector;
        private volatile boolean closed;
        TcpClientImpl owner;

        SelectorManager(TcpClientImpl ref) throws IOException {
//            super(null, null,
//                    "HermineTcpClient-" + ref.id + "-SelectorManager",
//                    0, false);
            owner = ref;
            selector = Selector.open();
        }
    }
}
