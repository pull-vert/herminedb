package org.hermine.adba.kotlin.internal

import jdk.incubator.sql2.DataSource
import jdk.incubator.sql2.DataSourceFactory
import org.hermine.adba.kotlin.newDataSourceFactory

fun getDataSource(url: String, user: String, pass: String): DataSource {
    return newDataSourceFactory<DataSourceFactory>("example.DataSourceFactory")
            .builder()
            .url(url)
            .username(user)
            .password(pass)
            .build()
}