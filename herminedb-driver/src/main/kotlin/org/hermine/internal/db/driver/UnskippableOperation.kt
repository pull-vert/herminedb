/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

internal class UnskippableOperation<T>(
        conn: SessionImpl,
        operationGroup: OperationGroupImpl<in T, *>,
        override val action: (AbstractOperation<T>) -> T
) : SimpleOperation<T>(conn, operationGroup, action) {

    override suspend fun operate() =
            try {
                invoke()
            } catch (ex: Throwable) {
                if (errorHandler != null) errorHandler?.invoke(ex)
                throw ex
            }
}
