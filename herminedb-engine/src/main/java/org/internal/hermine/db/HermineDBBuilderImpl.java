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

public class HermineDBBuilderImpl implements HermineDB.Builder {

    int dbPort = 35700;

    @Override
    public HermineDBBuilderImpl dbPort(int dbPort) {
        if (dbPort < 1) {
            throw new IllegalArgumentException("int must be greater or equals than 1");
        }
        this.dbPort = dbPort;
        return this;
    }

    @Override
    public HermineDB build() {
        return HermineDBImpl.create(this);
    }
}
