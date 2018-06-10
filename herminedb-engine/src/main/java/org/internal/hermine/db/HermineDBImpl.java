/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol,
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
package org.internal.hermine.db;

import org.hermine.db.HermineDB;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.channels.Selector;
import java.util.ArrayList;

final class HermineDBImpl extends HermineDB {

    // This reference is used to keep track of the facade HermineDB
    // that was returned to the application code.
    // It makes it possible to know when the application no longer
    // holds any reference to the HermineDB.
    // Unfortunately, this information is not enough to know when
    // to exit the SelectorManager thread. Because of the asynchronous
    // nature of the API, we also need to wait until all pending operations
    // have completed.
    private final WeakReference<HermineDBFacade> facadeRef;

    /**
     * This is a bit tricky:
     * 1. an HermineDBFacade has a final HermineDBImpl field.
     * 2. an HermineDBImpl has a final WeakReference<HermineDBFacade> field,
     *    where the referent is the facade created for that instance.
     * 3. We cannot just create the HermineDBFacade in the HermineDBImpl
     *    constructor, because it would be only weakly referenced and could
     *    be GC'ed before we can return it.
     * The solution is to use an instance of SingleFacadeFactory which will
     * allow the caller of new HermineDBImpl(...) to retrieve the facade
     * after the HermineDBImpl has been created.
     */
    private static final class SingleFacadeFactory {
        HermineDBFacade facade;
        HermineDBFacade createFacade(HermineDBImpl impl) {
            assert facade == null;
            return (facade = new HermineDBFacade(impl));
        }
    }

    static HermineDBFacade create(HermineDBBuilderImpl builder) {
        var facadeFactory = new SingleFacadeFactory();
        var impl = new HermineDBImpl(builder, facadeFactory);
        impl.start();
        assert facadeFactory.facade != null;
        assert impl.facadeRef.get() == facadeFactory.facade;
        return facadeFactory.facade;
    }

    private HermineDBImpl(HermineDBBuilderImpl builder, SingleFacadeFactory facadeFactory) {
        facadeRef = new WeakReference<>(facadeFactory.createFacade(this));

        assert facadeRef.get() != null;
    }

    private void start() {

    }

    // Main loop for this hermine db's selector
    private final static class SelectorManager extends Thread {

        private final Selector selector;
        private volatile boolean closed;
        HermineDBImpl owner;

        SelectorManager(HermineDBImpl ref) throws IOException {
//            super(null, null,
//                    "HttpClient-" + ref.id + "-SelectorManager",
//                    0, false);
            owner = ref;
            selector = Selector.open();
        }
    }
}
