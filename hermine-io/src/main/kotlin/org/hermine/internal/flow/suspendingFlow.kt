package org.hermine.internal.flow

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.sync.Mutex
import java.util.concurrent.Flow
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.coroutineContext

interface Source<out E> {
    suspend fun consume(sink: Sink<E>)
    companion object Factory
}

interface Sink<in E> {
    fun onConsume(consumer: Consumer)
    suspend fun send(item: E)
    fun close(cause: Throwable?)
}

interface Consumer {
    fun request(n: Long)
    fun cancel()
}

fun <E> Source<E>.toPublisher() = Flow.Publisher<E> { subscriber ->
    launch {
        val sinkSubscription = SinkSubscription(coroutineContext[Job]!!, coroutineContext, subscriber)
        subscriber.onSubscribe(sinkSubscription) // do it first, to avoid unnecessary suspensions
        this@toPublisher.consume(sinkSubscription)
    }
}

private class SinkSubscription<E>(
        val job: Job,
        val coroutineContext: CoroutineContext,
        val subscriber: Flow.Subscriber<in E>) : Sink<E> , Flow.Subscription {

    // Mutex is locked when either nRequested == 0 or while subscriber.onXXX is being invoked
    private val mutex = Mutex(locked = true)

    val _nRequested = atomic(0L) // < 0 when closed (CLOSED or SIGNALLED)

    // Sink methods

    public override suspend fun send(item: E) {
        // fast-path -- try send without suspension
        if (offer(item)) return
        // slow-path does suspend
        return sendSuspend(item)
    }

    override fun close(cause: Throwable?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // Flow.Subscription methods

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun request(n: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // private methods
    fun offer(item: E): Boolean {
        if (!mutex.tryLock()) return false
        doLockedNext(item)
        return true
    }

    private suspend fun sendSuspend(item: E) {
        mutex.lock()
        doLockedNext(item)
    }

    // assert: mutex.isLocked()
    private fun doLockedNext(item: E) {
        // check if job is already closed
        if (!job.isActive) {
            doLockedSignalCompleted()
            throw job.getCancellationException()
        }
        // notify subscriber
        try {
            subscriber.onNext(item)
        } catch (e: Throwable) {
            try {
                if (!job.cancel(e))
                    handleCoroutineException(coroutineContext, e)
            } finally {
                doLockedSignalCompleted()
            }
            throw job.getCancellationException()
        }
        // now update nRequested
        while (true) { // lock-free loop on nRequested
            val cur = _nRequested.value
            if (cur < 0) break // closed from inside onNext => unlock
            if (cur == Long.MAX_VALUE) break // no back-pressure => unlock
            val upd = cur - 1
            if (_nRequested.compareAndSet(cur, upd)) {
                if (upd == 0L) return // return to keep locked due to back-pressure
                break // unlock if upd > 0
            }
        }
        /*
           There is no sense to check for `isActive` before doing `unlock`, because cancellation/completion might
           happen after this check and before `unlock` (see `onCancellation` that does not do anything
           if it fails to acquire the lock that we are still holding).
           We have to recheck `isActive` after `unlock` anyway.
         */
        mutex.unlock()
        // recheck isActive
        if (!job.isActive && mutex.tryLock())
            doLockedSignalCompleted()
    }

    // assert: mutex.isLocked()
    private fun doLockedSignalCompleted() {
        try {
            if (_nRequested.value >= CLOSED) {
                _nRequested.value = SIGNALLED // we'll signal onError/onCompleted (that the final state -- no CAS needed)
                val cause = getCompletionCause()
                try {
                    if (cause != null && cause !is CancellationException)
                        subscriber.onError(cause)
                    else
                        subscriber.onComplete()
                } catch (e: Throwable) {
                    handleCoroutineException(coroutineContext, e)
                }
            }
        } finally {
            mutex.unlock()
        }
    }
}

fun <T> flowPublish(
        context: CoroutineContext = DefaultDispatcher,
        parent: Job? = null,
        block: suspend FlowPublisherScope<T>.() -> Unit
): Flow.Publisher<T> = object : Flow.Publisher<T> {

    override fun subscribe(subscriber: Flow.Subscriber<in T>) {
        launch {
            val scope = FlowPublisherScopeImpl<T>(this)
            object : Source<T> {
                override suspend fun consume(sink: Sink<T>) {
                    scope.sink = sink
                    block.invoke(FlowPublisherScopeImpl(this@launch, sink))
                }
            }
        }
    }
}

interface FlowPublisherScope<in E> : CoroutineScope, Sink<E> {
    /**
     * A reference to the Sink that this coroutine [sends][send] elements to.
     * It is provided for convenience, so that the code in the coroutine can refer
     * to the sink as `sink` as apposed to `this`.
     * All the [Sink] functions on this interface delegate to
     * the sink instance returned by this function.
     */
    val sink: Sink<E>
}

class FlowPublisherScopeImpl<E>(coroutineScope: CoroutineScope): FlowPublisherScope<E>, CoroutineScope by coroutineScope, Sink<E> {
    override lateinit var sink: Sink<E>

    override suspend fun send(item: E) {
        sink.send(item)
    }

    override fun close(cause: Throwable?) {
        sink.close(cause)
    }
}

private class FlowPublisherCoroutine<T>(
        parentContext: CoroutineContext,
        private val sink_: Sink<T>,
        private val subscriber: Flow.Subscriber<in T>
) : AbstractCoroutine<Unit>(parentContext, true), FlowPublisherScope<T>, Flow.Subscription, Sink<T> by sink_ {
    // Subscription methods

    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun request(n: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val sink: Sink<T>
        get() = this
}
