/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlin.coroutines.experimental.CoroutineContext

/**
 *
 * @param <T>
 */
internal open class SimpleOperation<T>(
        conn: ConnectionImpl,
        operationGroup: AbstractOperationGroup<in T, *>,
        open val action: (AbstractOperation<T>) -> T
) : AbstractOperation<T>(conn, operationGroup), () -> T {

    override fun follows(predecessor: Deferred<*>?, context: CoroutineContext) = async(context) {
        predecessor?.await()
        invoke()
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
