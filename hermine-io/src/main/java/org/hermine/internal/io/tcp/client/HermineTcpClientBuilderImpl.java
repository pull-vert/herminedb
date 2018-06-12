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

public class HermineTcpClientBuilderImpl implements HermineTcpClient.Builder {

    int port = 35700;

    @Override
    public HermineTcpClientBuilderImpl port(int port) {
        if (port < 1) {
            throw new IllegalArgumentException("int must be greater or equals than 1");
        }
        this.port = port;
        return this;
    }

    @Override
    public HermineTcpClient build() {
        return HermineTcpClientImpl.create(this);
    }
}