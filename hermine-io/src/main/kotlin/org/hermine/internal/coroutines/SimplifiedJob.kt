package org.hermine.internal.coroutines

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.loop
import kotlinx.coroutines.experimental.*
import kotlin.coroutines.experimental.CoroutineContext

/**
 * A concrete simplified implementation of [Job]. It is optionally a child to a parent job.
 * This job is cancelled when the parent is complete, but not vise-versa.
 *
 * This is an open class designed for extension by more specific classes that might augment the
 * state and mare store addition state information for completed jobs, like their result values.
 */
internal open class SimplifiedJob : Job {
    /*
       === Internal states ===

       state name    public state  description
       ------        ------------  -----------
       EMPTY         Active        no listeners
       SINGLE        Active        a single listener
       SINGLE_PLUS   Active        a single listener + NodeList added as its next
       LIST          Active        a list of listeners (promoted once, does not got back to JobNode/EmptyActive)
       COMPLETING    Completing    has a list of listeners (promoted once from LIST)
       CANCELLING    Cancelling    has a list of listeners (promoted once from LIST)
       FINAL_C       Cancelled     cancelled (final state)
       FINAL_F       Completed     failed for other reason (final state)
       FINAL_R       Completed     produced some result (final state)

       === Transitions ===

           Active states       Inactive states

                +---------+                          }
          --+-> | EMPTY   | ----+                    } Empty states
            |   +---------+     |                    }
            |     |     ^       |    +----------+
            |     |     |       +--> |  FINAL_* |
            |     V     |       |    +----------+
            |   +---------+     |                    }
            |   | SINGLE  | ----+                    } JobNode states
            |   +---------+     |                    }
            |        |          |                    }
            |        V          |                    }
            |  +-------------+  |                    }
            +- | SINGLE_PLUS |--+                    }
               +-------------+  |                    }
                     |          |
                     V          |
                +---------+     |                    }
                | LIST    | ----+                    } NodeList states
                +---------+     |                    }
                   |   |        |
          +--------+   |        |
          |            V        |
          |    +------------+   |    +------------+  }
          |    | COMPLETING | --+--> | CANCELLING |  } Finishing states
          |    +------------+        +------------+  }
          |          |                    ^
          |          |                    |
          +----------+--------------------+
     */
    final override val key: CoroutineContext.Key<*> get() = Job

    private val _state = atomic(SimplifiedJobLifecycle.EMPTY.value)

    @Volatile
    private var parentHandle: DisposableHandle? = null

    @Volatile
    private var cancelled: Cancelled? = null

    // ------------ initialization ------------

    /**
     * Initializes parent job.
     * It shall be invoked at most once after construction after all other initialization.
     * @suppress **This is unstable API and it is subject to change.**
     */
    internal fun initParentJobInternal(parent: Job?) {
        check(parentHandle == null)
        if (parent == null) {
            parentHandle = NonDisposableHandle
            return
        }
        parent.start() // make sure the parent is started
        @Suppress("DEPRECATION")
        val handle = parent.attachChild(this)
        parentHandle = handle
        // now check our state _after_ registering (see updateState order of actions)
        if (isCompleted) {
            handle.dispose()
            parentHandle = NonDisposableHandle // release it just in case, to aid GC
        }
    }

    /**
     * Returns current state of this job.
     */
    internal val state: SimplifiedJobLifecycle
        get() {
            _state.loop { value ->
                // helper loop on state (complete in-progress atomic operations)
                return getStateByValue(value)
            }
        }

    public final override val isActive: Boolean
        get() {
            val state = this.state
            return state.isActive
        }

    public final override val isCompleted: Boolean get() = !isActive

    public final override val isCancelled: Boolean
        get() {
            val state = this.state
            return state.isCancelled
        }

    public final override fun start() = false // already started by default

    public final override fun getCancellationException(): CancellationException {
        val state = this.state
        return when {
            state is Finishing && cancelled != null ->
                cancelled.cause.toCancellationException("Job is being cancelled")
            state is Incomplete ->
                error("Job was not completed or cancelled yet: $this")
            state is CompletedExceptionally ->
                state.cause.toCancellationException("Job has failed")
            else -> JobCancellationException("Job has completed normally", null, this)
        }
    }

    internal enum class SimplifiedJobLifecycle(val value: Int) {
        EMPTY(0),
        SINGLE(1),
        SINGLE_PLUS(2),
        LIST(3),
        COMPLETING(4),
        CANCELLING(5),
        FINAL_C(6),
        FINAL_F(7),
        FINAL_R(8);

        /**
         * @return true if Active (not Final)
         */
        val isActive: Boolean
            get() = this != FINAL_C && this != FINAL_F && this != FINAL_R

        val isCancelled
            get() = this == FINAL_C
    }

    private fun getStateByValue(value: Int) = SimplifiedJobLifecycle.values().find { it.value == value }
            ?: throw IllegalStateException("$value do not match existing SimplifiedJobLifecycle")

    private fun Throwable.toCancellationException(message: String): CancellationException =
            this as? CancellationException ?: JobCancellationException(message, this, this@SimplifiedJob)
}

@PublishedApi internal const val MODE_ATOMIC_DEFAULT = 0 // schedule non-cancellable dispatch for suspendCoroutine
@PublishedApi internal const val MODE_CANCELLABLE = 1    // schedule cancellable dispatch for suspendCancellableCoroutine
@PublishedApi internal const val MODE_DIRECT = 2         // when the context is right just invoke the delegate continuation direct
@PublishedApi internal const val MODE_UNDISPATCHED = 3   // when the thread is right, but need to mark it with current coroutine
@PublishedApi internal const val MODE_IGNORE = 4         // don't do anything

/**
 * A specific subclass of [CompletedExceptionally] for cancelled jobs.
 *
 * **Note: This class cannot be used outside of internal coroutines framework**.
 *
 * @param job the job that was cancelled.
 * @param cause the exceptional completion cause. If `cause` is null, then a [JobCancellationException] is created.
 * @suppress **This is unstable API and it is subject to change.**
 */
internal class Cancelled(
        job: Job,
        cause: Throwable?
) : CompletedExceptionally(cause ?: JobCancellationException("Job was cancelled normally", null, job))
