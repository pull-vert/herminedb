/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol. Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import jdk.incubator.sql2.*
import jdk.incubator.sql2.Session.Lifecycle
import java.time.Duration
import java.util.function.Consumer
import java.util.function.LongConsumer
import kotlin.coroutines.experimental.CoroutineContext

internal class SessionImpl(
        dataSource: DataSourceImpl,
        properties: Map<SessionProperty, Any>
) : OperationGroupImpl<Any?, Any>(), Session {

    override val context: CoroutineContext

    init {
        val execProp = CONTEXT
        context = properties.getOrDefault(execProp, execProp.defaultValue()) as CoroutineContext
    }

    // PUBLIC
    override fun attachOperation(): AbstractOperation<Void?> {
        if (!isHeld()) throw IllegalStateException("TODO")
        return SimpleOperation(this, this, this::hermineConnect)
    }

    override fun validationOperation(depth: Session.Validation?): Operation<Void> {
        if (!isHeld()) throw IllegalStateException("TODO")
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun closeOperation(): Operation<Void?> {
        if (!isHeld()) throw IllegalStateException("TODO")
        return UnskippableOperation(this, this, this::hermineClose)
    }

    override fun <S : Any?, T : Any?> operationGroup(): OperationGroup<S, T> {
        if (!isHeld()) throw IllegalStateException("TODO")
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun transactionCompletion(): TransactionCompletion {
        if (!isHeld()) throw IllegalStateException("TODO")
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun registerLifecycleListener(listener: Session.SessionLifecycleListener?): Session {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deregisterLifecycleListener(listener: Session.SessionLifecycleListener?): Session {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSessionLifecycle(): Lifecycle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun abort(): Session {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getProperties(): MutableMap<SessionProperty, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun shardingKeyBuilder(): ShardingKey.Builder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun requestHook(request: LongConsumer?): Session {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun activate(): Session {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deactivate(): Session {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // Override Operation methods

    override fun timeout(minTime: Duration): SessionImpl {
        super.timeout(minTime)
        return this
    }

    override fun onError(handler: Consumer<Throwable>): SessionImpl {
        super.onError(handler)
        return this
    }

    // INTERNAL
    protected fun setLifecycle(next: Lifecycle): SessionImpl {
        return this
    }

    private fun hermineConnect(op: AbstractOperation<Void?>): Void? {
        return null
    }

    private fun hermineClose(op: AbstractOperation<Void?>): Void? {
        return null
    }
}
