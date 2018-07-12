/*
 * Copyright 2018 HermineDB's author : Frédéric Montariol
 * and explicitly declared author of each file if specified.
 * Use of this source code is governed by the Apache 2.0 license.
 */

package org.hermine.internal.db.driver

import jdk.incubator.sql2.*
import jdk.incubator.sql2.Connection.Lifecycle
import java.time.Duration
import java.util.function.Consumer
import kotlin.coroutines.experimental.CoroutineContext

internal class ConnectionImpl(
        dataSource: DataSourceImpl,
        properties: Map<ConnectionProperty, Any>
) : OperationGroupImpl<Any?, Any>(), Connection {

    override val context: CoroutineContext

    init {
        val execProp = CONTEXT
        context = properties.getOrDefault(execProp, execProp.defaultValue()) as CoroutineContext
    }

    // PUBLIC
    override fun connectOperation(): AbstractOperation<Void?> {
        if (!isHeld()) throw IllegalStateException("TODO")
        return SimpleOperation(this, this, this::hermineConnect)
    }

    override fun validationOperation(depth: Connection.Validation?): Operation<Void> {
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

    override fun transaction(): Transaction {
        if (!isHeld()) throw IllegalStateException("TODO")
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun registerLifecycleListener(listener: Connection.ConnectionLifecycleListener?): Connection {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deregisterLifecycleListener(listener: Connection.ConnectionLifecycleListener?): Connection {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getConnectionLifecycle(): Lifecycle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun abort(): Connection {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getProperties(): MutableMap<ConnectionProperty, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun shardingKeyBuilder(): ShardingKey.Builder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun requestHook(request: Consumer<Long>?): Connection {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun activate(): Connection {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deactivate(): Connection {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // Override Operation methods

    override fun timeout(minTime: Duration): ConnectionImpl {
        super.timeout(minTime)
        return this
    }

    override fun onError(handler: Consumer<Throwable>): ConnectionImpl {
        super.onError(handler)
        return this
    }

    // INTERNAL
    protected fun setLifecycle(next: Lifecycle): ConnectionImpl {
        return this
    }

    private fun hermineConnect(op: AbstractOperation<Void?>): Void? {
        return null
    }

    private fun hermineClose(op: AbstractOperation<Void?>): Void? {
        return null
    }
}
