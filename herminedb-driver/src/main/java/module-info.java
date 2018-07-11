/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

module org.herminedb.driver {
    requires jdk.incubator.adba;
    requires org.apache.logging.log4j;
    requires org.slf4j;
    requires kotlin.stdlib;
    requires kotlin.logging;
    requires kotlinx.coroutines.core;
    requires kotlinx.coroutines.jdk8;
    exports org.hermine.db.driver;
    provides jdk.incubator.sql2.DataSourceFactory with org.hermine.db.driver.HermineDataSourceFactory;
}
