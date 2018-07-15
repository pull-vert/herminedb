/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.adba.kotlin

import jdk.incubator.sql2.DataSourceFactory
import jdk.incubator.sql2.Result

fun <T : DataSourceFactory> newDataSourceFactory(name: String): T = DataSourceFactory.newFactory(name) ?: throw ClassNotFoundException("DataSourceFactory $name not found")

inline fun <reified T : Any> Result.Column.getAt(id: String) = at(id).get(T::class.java)

// todo find better syntax !!
//fun ParameterizedOperation<*>.setPair(id: String, function: () -> Pair<Any, SqlType>) {
//    val (value, type) = function.invoke()
//    set(id, value, type)
//}
