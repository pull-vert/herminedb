/*
 * Copyright 2002-2018 the original author or authors.
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

/**
 * An HermineDBFacade is a simple class that wraps an HermineDB implementation
 * and delegates everything to its implementation delegate.
 */
final class HermineDBFacade extends HermineDB {

    final HermineDBImpl impl;

    /**
     * Creates an HermineDBFacade.
     */
    HermineDBFacade(HermineDBImpl impl) {
        this.impl = impl;
    }

    @Override
    public String toString() {
        return impl.toString();
    }
}
