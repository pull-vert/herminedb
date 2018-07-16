/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlin.coroutines.experimental.CoroutineContext

/**
 *
 * @param <T>
 */
internal open class SimpleOperation<T>(
        conn: SessionImpl,
        operationGroup: OperationGroupImpl<in T, *>,
        open val action: (AbstractOperation<T>) -> T
) : AbstractOperation<T>(conn, operationGroup), () -> T {

    override fun next(channel: Channel<T>, context: CoroutineContext) = async(context) {
        val value = invoke()
        channel.send(value)
        value
    }

    /**
     * Computes the value of this Operation by calling the action. If this
     * Operation has been canceled throws SqlSkippedException. If the action
     * throws a checked exception, wrap that checked exception in a SqlException.
     * SqlException is unchecked as required by Supplier, and can be handled by
     * CompletionStage.
     */
    override fun invoke(): T {
        checkCanceled()
        try {
            return action(this)
        } finally {
            operationLifecycle = OperationLifecycle.COMPLETED
        }
    }
}
