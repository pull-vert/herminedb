/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import mu.KotlinLogging
import java.time.Duration
import java.util.function.Consumer
import java.util.function.Function
import kotlin.coroutines.experimental.CoroutineContext

// Place definition above class declaration to make field static
private val logger = KotlinLogging.logger {}

internal open class SequentialOperationGroup<S, T> : AbstractOperationGroup<S, T> {

    // used only by Session. Will break if used by any other class.
    constructor() : super()

    constructor(conn: ConnectionImpl, group: AbstractOperationGroup<in T, *>) : super(conn, group)

    /**
     * completed when this OperationGroup is no longer held. Completion of this
     * OperationGroup depends on held.
     *
     * @see #submit,
     * @see #releaseProhibitingMoreMembers,
     * @see #submitHoldingForMoreMembers
     */
    private val held: CompletableDeferred<Unit> = CompletableDeferred()

    override fun releaseProhibitingMoreMembers(): SubmissionImpl<T> {
        logger.debug{"SequentialOperationGroup#releaseProhibitingMoreMembers"}
        if (!isImmutable() || !isHeld()) throw IllegalStateException("TODO")
        held.complete(Unit)
        immutable()  // having set isHeld to false this call will make this OpGrp immutable
        return submission!!
    }

    // Override Operation methods

    override fun timeout(minTime: Duration): SequentialOperationGroup<S, T> {
        super.timeout(minTime)
        return this
    }

    override fun onError(handler: Consumer<Throwable>): SequentialOperationGroup<S, T> {
        super.onError(handler)
        return this
    }

    override fun submit(): SubmissionImpl<T> {
        logger.debug{"SequentialOperationGroup#submit"}
        if (isImmutable()) throw IllegalStateException("TODO")
        accumulator = collector.supplier().get()
        held.complete(Unit)
        return super.submit()
    }

    // Internal methods

    override fun follows(predecessor: Deferred<*>?, context: CoroutineContext): Deferred<T?> {
        logger.debug{"SequentialOperationGroup#follows"}
        return async(context) {
            val cond = condition.await()
            if (cond) {
                head.complete(predecessor)
                held.await()
                (collector.finisher() as Function<Any?, Any>)
                        .apply(accumulator) as T
            } else {
                null
            }
        }
    }

    override fun isHeld() = !held.isCompleted
}
