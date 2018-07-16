/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import mu.KotlinLogging
import kotlin.coroutines.experimental.CoroutineContext

// Place definition above class declaration to make field static
private val logger = KotlinLogging.logger {}

internal class UnskippableOperation<T>(
        conn: SessionImpl,
        operationGroup: OperationGroupImpl<in T, *>,
        override val action: (AbstractOperation<T>) -> T
): SimpleOperation<T>(conn, operationGroup, action) {

    override fun next(channel: Channel<T>, context: CoroutineContext) = async(context) {
        try {
            val value = invoke()
            channel.send(value)
            value
        } catch (ex: Throwable) {
            if (errorHandler != null) errorHandler?.invoke(ex)
            throw ex
        }
    }
}
