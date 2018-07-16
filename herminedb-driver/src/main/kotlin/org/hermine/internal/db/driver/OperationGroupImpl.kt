/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import jdk.incubator.sql2.*
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.future.asDeferred
import mu.KotlinLogging
import java.time.Duration
import java.util.concurrent.CompletionStage
import java.util.function.*
import java.util.function.Function
import java.util.logging.Logger
import java.util.stream.Collector
import kotlin.coroutines.experimental.CoroutineContext

// Place definition above class declaration to make field static
private val logger = KotlinLogging.logger {}

private val DEFAULT_COLLECTOR: Collector<*, *, *> = Collector.of(
        Supplier { null },
        BiConsumer { _, _: Any? -> },
        BinaryOperator { _, _ -> null },
        Function { _: Nothing? -> null })

private val DEFAULT_CONDITION = CompletableDeferred(true)

internal open class OperationGroupImpl<S, T> : AbstractOperation<T>, OperationGroup<S, T> {

    // used only by Session. Will break if used by any other class.
    constructor() : super()

    constructor(conn: SessionImpl, group: OperationGroupImpl<in T, *>) : super(conn, group)

    protected var condition: Deferred<Boolean> = DEFAULT_CONDITION

    lateinit var submission: SubmissionImpl<T>
    var accumulator: Any? = null

    var isIndependent: Boolean = false
    var isParallel: Boolean = false
    protected var collector: Collector<*, *, *> = DEFAULT_COLLECTOR

    /**
     * Completed when this OperationGroup is no longer held. Completion of this
     * OperationGroup depends on held.
     *
     * @see #submit,
     * @see #releaseProhibitingMoreMembers,
     * @see #submitHoldingForMoreMembers
     */
    private val held: CompletableDeferred<Unit> = CompletableDeferred()

    /**
     * Channel of all submitted member Operations. Mutable until not isHeld().
     */
    protected var operationChannel: Channel<S> = Channel()

    override fun parallel(): OperationGroupImpl<S, T> {
        if (isImmutable() || isParallel) throw IllegalStateException("TODO")
        isParallel = true
        return this
    }

    override fun independent(): OperationGroupImpl<S, T> {
        if (isImmutable() || isIndependent) throw IllegalStateException("TODO")
        isIndependent = true
        return this
    }

    override fun conditional(condition: CompletionStage<Boolean>?): OperationGroupImpl<S, T> {
        if (isImmutable() || condition == null) throw IllegalStateException("TODO")
        this.condition = condition.asDeferred()
        return this
    }

    override fun collect(c: Collector<S, *, T>?): OperationGroupImpl<S, T> {
        if (isImmutable() || collector !== DEFAULT_COLLECTOR) throw IllegalStateException("TODO")
        if (c == null) throw IllegalArgumentException("TODO")
        collector = c
        return this
    }

    override fun submitHoldingForMoreMembers(): SubmissionImpl<T> {
        if (isImmutable() || !isHeld()) throw IllegalStateException("TODO")  //TODO prevent multiple calls
        accumulator = collector.supplier().get()
        submission = super.submit()
        return submission
    }

    override fun catchOperation(): AbstractOperation<S> {
        if (!isHeld()) throw IllegalStateException("TODO")
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun <R : S> arrayRowCountOperation(sql: String?): ArrayRowCountOperation<R> {
        if (!isHeld()) throw IllegalStateException("TODO")
        if (sql == null) throw IllegalArgumentException("TODO")
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun <R : S> rowCountOperation(sql: String?): ParameterizedRowCountOperation<R> {
        if (!isHeld()) throw IllegalStateException("TODO")
        if (sql == null) throw IllegalArgumentException("TODO")
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun operation(sql: String?): AbstractOperation<S> {
        if (!isHeld()) throw IllegalStateException("TODO")
        if (sql == null) throw IllegalArgumentException("TODO")
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun <R : S> outOperation(sql: String?): OutOperation<R> {
        if (!isHeld()) throw IllegalStateException("TODO")
        if (sql == null) throw IllegalArgumentException("TODO")
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun <R : S> rowOperation(sql: String?): ParameterizedRowOperation<R> {
        if (!isHeld()) throw IllegalStateException("TODO")
        if (sql == null) throw IllegalArgumentException("TODO")
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun <R : S> rowPublisherOperation(sql: String?): ParameterizedRowPublisherOperation<R> {
        if (!isHeld()) throw IllegalStateException("TODO")
        if (sql == null) throw IllegalArgumentException("TODO")
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun <R : S> multiOperation(sql: String?): MultiOperation<R> {
        if (!isHeld()) throw IllegalStateException("TODO")
        if (sql == null) throw IllegalArgumentException("TODO")
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun endTransactionOperation(trans: TransactionCompletion): AbstractOperation<TransactionOutcome> {
        if (!isHeld()) throw IllegalStateException("TODO")
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun <R : S> localOperation(): LocalOperation<R> {
        if (!isHeld()) throw IllegalStateException("TODO")
        throw UnsupportedOperationException("Not supported yet.")
    }

    override fun logger(logger: Logger): OperationGroup<S, T> {
        throw UnsupportedOperationException("Will use other Logger")
    }

    override fun releaseProhibitingMoreMembers(): SubmissionImpl<T> {
        logger.debug { "SequentialOperationGroup#releaseProhibitingMoreMembers" }
        if (!isImmutable() || !isHeld()) throw IllegalStateException("TODO")
        held.complete(Unit)
        immutable()  // having set isHeld to false this call will make this OpGrp immutable
        return submission!!
    }

    // Override Operation methods

    override fun timeout(minTime: Duration): OperationGroupImpl<S, T> {
        super.timeout(minTime)
        return this
    }

    override fun onError(handler: Consumer<Throwable>): OperationGroupImpl<S, T> {
        super.onError(handler)
        return this
    }

    override fun submit(): SubmissionImpl<T> {
        logger.debug { "OperationGroupImpl#submitOperation" }
        if (isImmutable()) throw IllegalStateException("TODO")
        accumulator = collector.supplier().get()
        held.complete(Unit)
        return super.submit()
    }

    // Internal methods

    internal fun submitOperation(op: AbstractOperation<S>): SubmissionImpl<S> {
        logger.debug { "OperationGroupImpl#submitOperation" }
        return SubmissionImpl(this::cancel,
                op.attachErrorHandler(op.next(operationChannel, context)))
    }

    override fun next(channel: Channel<T>, context: CoroutineContext): Deferred<T?> {
        logger.debug { "OperationGroupImpl#next" }
        return async(context) {
            val cond = condition.await()
            if (cond) {
                held.await() // wait until submit or releaseProhibitingMoreMembers
                (collector.finisher() as Function<Any?, Any>)
                        .apply(accumulator) as T
            } else {
                null
            }
        }
    }

    fun isHeld() = !held.isCompleted
}
