/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.db.driver

import jdk.incubator.sql2.DataSource
import jdk.incubator.sql2.DataSourceFactory
import org.hermine.internal.db.driver.DataSourceBuilderImpl

class HermineDataSourceFactory : DataSourceFactory {
    override fun builder(): DataSource.Builder = DataSourceBuilderImpl()
}
