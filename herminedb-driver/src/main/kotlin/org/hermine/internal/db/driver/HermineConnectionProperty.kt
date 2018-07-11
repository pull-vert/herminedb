/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import jdk.incubator.sql2.ConnectionProperty
import kotlinx.coroutines.experimental.DefaultDispatcher
import java.util.function.Function
import kotlin.coroutines.experimental.CoroutineContext

internal object CONTEXT : HermineConnectionProperty(
        "CONTEXT",
        CoroutineContext::class.java,
        Function { v -> v is CoroutineContext },
        DefaultDispatcher,
        false)

internal abstract class HermineConnectionProperty(
        val name: String,
        val range: Class<*>,
        val validator: Function<Any, Boolean>,
        val defaultValue: Any,
        val sensitive: Boolean) : ConnectionProperty {

    override fun name() = name

    override fun range(): Class<*> {
        return range
    }

    override fun validate(value: Any): Boolean {
        return validator.apply(value)
    }

    override fun defaultValue(): Any {
        return defaultValue
    }

    override fun isSensitive(): Boolean {
        return sensitive
    }
}
