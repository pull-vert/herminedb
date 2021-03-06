/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol. Use of this source code is governed by the Apache 2.0 license.
 */

module org.hermine.io {
    requires atomicfu;
    requires kotlin.stdlib;
    requires kotlinx.coroutines.core;
    exports org.hermine.io.tcp.client;
}
