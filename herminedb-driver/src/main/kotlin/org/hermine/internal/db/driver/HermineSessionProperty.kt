/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol. Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import jdk.incubator.sql2.SessionProperty
import kotlinx.coroutines.Dispatchers
import java.util.function.Function
import kotlin.coroutines.CoroutineContext

internal object CONTEXT : HermineSessionProperty(
        "CONTEXT",
        CoroutineContext::class.java,
        Function { v -> v is CoroutineContext },
        Dispatchers.Default,
        false)

internal abstract class HermineSessionProperty(
        val name: String,
        val range: Class<*>,
        val validator: Function<Any, Boolean>,
        val defaultValue: Any,
        val sensitive: Boolean) : SessionProperty {

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
