package org.hermine.internal.flow

import kotlinx.coroutines.experimental.*
import java.util.concurrent.Flow
import kotlin.coroutines.experimental.CoroutineContext

interface Source<out E> {
    suspend fun consume(sink: Sink<E>)
    companion object Factory
}

interface Sink<in E> {
    suspend fun send(item: E)
    fun close(cause: Throwable?)
}

//class SinkSubscriber<E>(val sink: Sink<E>, val subscriber: Flow.Subscriber<in E>) : Sink<E> by sink, Flow.Subscriber<E> by subscriber

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
