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
package org.hermine.db;

import org.internal.hermine.db.HermineDBBuilderImpl;

import javax.net.ssl.SSLContext;

public abstract class HermineDB {

    /**
     * Creates an HermineDB.
     */
    protected HermineDB() {}

    /**
     * Returns a new {@code HermineDB} with default settings.
     *
     * <p> Equivalent to {@code newBuilder().build()}.
     *
     * <p> The default settings include: the "GET" request method, a preference
     * of {@linkplain SSLContext#getDefault() default SSL context}.
     *
     * @implNote The system-wide default values are retrieved at the time the
     * {@code HermineDB} instance is constructed. Changing the system-wide
     * values after an {@code HermineDB} instance has been built, for
     * instance, by calling {@link SSLContext#setDefault(SSLContext)}, has no effect on already
     * built instances.
     *
     * @return a new HermineDB
     */
    public static HermineDB newHermineDB() {
        return newBuilder().build();
    }

    /**
     * Creates a new {@code HermineDB} builder.
     *
     * @return an {@code HermineDB.Builder}
     */
    public static Builder newBuilder() {
        return new HermineDBBuilderImpl();
    }

    /**
     * A builder of {@linkplain HermineDB Hermine DB}.
     *
     * <p> Builders are created by invoking {@link HermineDB#newBuilder()
     * newBuilder}. Each of the setter methods modifies the state of the builder
     * and returns the same instance. Builders are not thread-safe and should not be
     * used concurrently from multiple threads without external synchronization.
     */
    public interface Builder {

        /**
         * Sets the TCP port HermineDB server will listen to
         *
         * @param dbPort HermineDB TCP port
         * @return this builder
         * @throws IllegalArgumentException if the given priority is out of range
         */
        Builder dbPort(int dbPort);

        /**
         * Returns a new {@link HermineDB} built from the current state of this
         * builder.
         *
         * @return a new {@code HermineDB}
         */
        HermineDB build();
    }
}
