/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol. Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

/**
 *
 * @param <T>
 */
internal open class SimpleOperation<T>(
        conn: SessionImpl,
        operationGroup: OperationGroupImpl<in T, *>,
        open val action: (AbstractOperation<T>) -> T
) : AbstractOperation<T>(conn, operationGroup), () -> T {

    override suspend fun operate() = invoke()

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
