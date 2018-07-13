/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import jdk.incubator.sql2.AdbaType
import jdk.incubator.sql2.Operation
import jdk.incubator.sql2.SqlSkippedException
import jdk.incubator.sql2.SqlType
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.channels.Channel
import mu.KotlinLogging
import java.math.BigInteger
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.function.Consumer
import kotlin.coroutines.experimental.CoroutineContext

// Place definition above class declaration to make field static
private val logger = KotlinLogging.logger {}

internal abstract class AbstractOperation<T> : Operation<T> {

    // attributes
    private var timeout: Duration? = null
    protected var errorHandler: ((Throwable) -> Unit)? = null

    // internal state
    private val connection: ConnectionImpl
    private val group: OperationGroupImpl<T, *>
    protected var operationLifecycle = OperationLifecycle.MUTABLE

    // used only by Session
    constructor () {
        connection = this as ConnectionImpl
        group = this as OperationGroupImpl<T, *>
    }

    constructor(connection: ConnectionImpl, group: OperationGroupImpl<*, *>) {
        this.connection = connection
        this.group = group as OperationGroupImpl<T, *>
    }

    override fun onError(handler: Consumer<Throwable>?): AbstractOperation<T> {
        if (isImmutable() || errorHandler != null) {
            throw IllegalStateException("TODO")
        }
        if (handler == null) {
            throw IllegalArgumentException("TODO")
        }
        errorHandler = handler::accept
        return this
    }

    override fun timeout(minTime: Duration?): AbstractOperation<T> {
        if (isImmutable() || timeout != null) {
            throw IllegalStateException("TODO")
        }
        if (null == minTime || minTime.isNegative || minTime.isZero) {
            throw IllegalArgumentException("minTime must be > 0")
        }
        timeout = minTime
        return this
    }

    /**
     * Returns true if this Operation is immutable. An Operation is immutable if
     * it has been submitted. Held OperationGroups are an exception.
     *
     * @return return true if immutable
     */
    fun isImmutable() = operationLifecycle.isImmutable

    protected fun immutable(): AbstractOperation<T> {
        operationLifecycle = OperationLifecycle.RELEASED
        return this
    }

    fun getTimeoutMillis() = timeout?.get(ChronoUnit.MILLIS) ?: 0L

    override fun submit(): SubmissionImpl<T> {
        logger.debug{"AbstractOperation#submitOperation"}
        if (isImmutable()) {
            throw IllegalStateException("TODO")
        }
        immutable()
        return group.submitOperation(this)
    }

    open val context: CoroutineContext
        get() = connection.context


    /**
     * Attaches the Deferred that starts this Operation to the predecessor and
     * return a Deferred that represents completion of this Operation.
     * The returned Deferred may not be directly attached to the predecessor,
     * but completion of the predecessor should result in completion of the returned
     * Deferred. (Note: Not quite true for OperationGroups submitted by
     * calling submitHoldingForMoreMembers. While the returned Deferred
     * does depend on the predecessor, it also depends on user code calling
     * releaseProhibitingMoreMembers.)
     *
     * @param predecessor the predecessor of this operation. Completion of predecessor starts
     * execution of this Operation
     * @param context used for asynchronous execution
     * @return completion of this Deferred means this Operation is complete.
     * The result value of the Operation is the value of the Deferred.
     */
    internal abstract fun next(channel: Channel<T>, context: CoroutineContext): Deferred<T?>

    protected fun cancel(): Boolean {
        if (operationLifecycle.isFinished) {
            return false
        } else {
            operationLifecycle = OperationLifecycle.CANCELED
            return true
        }
    }

    private fun isCanceled() = operationLifecycle.isCanceled

    protected fun checkCanceled(): AbstractOperation<T> {
        if (isCanceled()) {
            throw SqlSkippedException("TODO", null, null, -1, null, -1)
        }
        return this
    }

    /**
     * If an errorHandler is specified, invoke it in event the argument completes
     * exceptionally. If there is no errorHandle specified, return the argument.
     *
     * @param result A Deferred that may complete exceptionally
     * @return a Deferred that will call the errorHandle if any.
     */
    internal fun attachErrorHandler(result: Deferred<T?>): Deferred<T?> {
        logger.debug{"AbstractOperation#attachErrorHandler"}
        if (null != errorHandler) {
            result.invokeOnCompletion {
                it?.let {
                    val ex = it.unwrapException()
                    errorHandler?.invoke(it)
                    if (ex is SqlSkippedException) throw ex
                    else throw SqlSkippedException("TODO", ex, null, -1, null, -1)
                }
            }
        }
        return result
    }

    protected enum class OperationLifecycle {
        MUTABLE,
        HELD,
        RELEASED,
        COMPLETED,
        CANCELED;

        /**
         * @return true if op has been submitted which means no more configuration
         */
        val isSubmitted: Boolean
            get() = this != MUTABLE

        /**
         * @return return true if no new members may be added. Implies isSubmitted
         */
        //TODO better name?
        val isImmutable: Boolean
            get() = this == RELEASED || this == COMPLETED || this == CANCELED

        val isFinished: Boolean
            get() = this == COMPLETED || this == CANCELED

        val isCanceled: Boolean
            get() = this == CANCELED
    }
}

private val CLASS_TO_ADBATYPE = mapOf<Class<*>, AdbaType>(
        BigInteger::class.java to AdbaType.BIGINT,
        Boolean::class.java to AdbaType.BOOLEAN,
        Byte::class.java to AdbaType.TINYINT,
        Class.forName("[B") to AdbaType.VARBINARY,
        Character::class.java to AdbaType.CHAR,
        Double::class.java to AdbaType.DOUBLE,
        Float::class.java to AdbaType.FLOAT,
        Integer::class.java to AdbaType.INTEGER,
        LocalDate::class.java to AdbaType.DATE,
        LocalDateTime::class.java to AdbaType.TIMESTAMP,
        LocalTime::class.java to AdbaType.TIME,
        OffsetDateTime::class.java to AdbaType.TIMESTAMP_WITH_TIME_ZONE,
        OffsetTime::class.java to AdbaType.TIME_WITH_TIME_ZONE,
        Short::class.java to AdbaType.SMALLINT,
        String::class.java to AdbaType.VARCHAR
)

/**
 * Find the default SQLType to represent a Java type.
 *
 * @param c a Java type
 * @return the default SQLType to represent the Java type
 */
internal fun toSQLType(c: Class<*>): SqlType {
    return CLASS_TO_ADBATYPE[c] ?: throw UnsupportedOperationException("${c.simpleName} is not Not supported yet.")
}
