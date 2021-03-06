/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol. Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import jdk.incubator.sql2.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.future.asDeferred
import mu.KotlinLogging
import java.time.Duration
import java.util.concurrent.CompletionStage
import java.util.function.*
import java.util.function.Function
import java.util.logging.Logger
import java.util.stream.Collector

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

    constructor(session: SessionImpl, group: OperationGroupImpl<T, *>) : super(session, group)

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
    private val held = CompletableDeferred<Unit>()

    private val parentJob = Job()

    /**
     * Last operation. Mutable until not isHeld().
     */
    protected var lastOp: Deferred<S?>? = null

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
//        return ParameterizedRowCountOperationImpl(session, this, sql)
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
        logger.debug { "OperationGroupImpl#submit" }
        if (isImmutable()) throw IllegalStateException("TODO")
        accumulator = collector.supplier().get()
        held.complete(Unit)
        // start all operations
        startOpJobs()
        return super.submit()
    }

    override fun close() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // Internal methods

    internal fun submitOperation(op: AbstractOperation<S>): SubmissionImpl<S> {
        logger.debug { "OperationGroupImpl#submitOperation" }
        val newLastOp = op.follows(lastOp, context, isParallel, isIndependent, parentJob)
        lastOp = newLastOp
        return SubmissionImpl(this::cancel, newLastOp)
    }

    override suspend fun operate(): T? {
        logger.debug { "OperationGroupImpl#operate" }
        return if (condition.await()) {
            held.join() // wait until submit or releaseProhibitingMoreMembers

            (collector.finisher() as Function<Any?, Any>)
                    .apply(accumulator) as T
        } else {
            null
        }
    }

    fun isHeld() = !held.isCompleted

    private fun startOpJobs() {
        for ((index, opJob) in parentJob.children.withIndex()) {
            val start = opJob.start() // true if job was not started, false if job was already started or completed
            logger.debug { "start $index child operation job = $start" }
        }
    }
}
