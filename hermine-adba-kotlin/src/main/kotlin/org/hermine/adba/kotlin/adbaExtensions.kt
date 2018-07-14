package org.hermine.adba.kotlin

import jdk.incubator.sql2.DataSourceFactory

fun <T : DataSourceFactory> newDataSourceFactory(name: String): T = DataSourceFactory.newFactory(name) ?: throw ClassNotFoundException("DataSourceFactory $name not found")