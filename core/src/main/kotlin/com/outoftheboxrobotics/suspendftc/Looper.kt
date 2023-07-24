package com.outoftheboxrobotics.suspendftc

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.*

/**
 * Event looper that can run coroutines. Usually one per thread.
 */
class Looper {
    internal data class LooperContext(val looper: Looper) : AbstractCoroutineContextElement(LooperContext) {
        companion object Key : CoroutineContext.Key<LooperContext>
    }

    private val tasks = Channel<() -> Unit>(capacity = Channel.UNLIMITED)
    internal val context = Dispatchers.Unconfined + LooperContext(this)
    private val looperScope = CoroutineScope(context)

    /**
     * Starts running scheduled coroutines. Will suspend until [cancel] is called.
     */
    suspend fun run() {
        try {
            while (coroutineContext.isActive) {
                tasks.receiveCatching().getOrNull()?.invoke() ?: return
            }
        } finally {
            looperScope.cancel()
        }
    }

    /**
     * Stops the event looper and any scheduled coroutines.
     */
    fun cancel() {
        tasks.close()
    }

    internal fun addTask(task: () -> Unit) {
        tasks.trySend(task)
    }

    /**
     * Schedules a coroutine to run on the looper.
     *
     * This can be called before the looper starts or while the looper is running. Other running coroutines can also
     * schedule new coroutines.
     */
    fun scheduleCoroutine(action: suspend CoroutineScope.() -> Unit): Job {
        val job = looperScope.launch(start = CoroutineStart.LAZY) { action() }

        object : Continuation<Unit> {
            override val context = looperScope.coroutineContext
            override fun resumeWith(result: Result<Unit>) = Unit
        }.let { cont ->
            addTask {
                suspend {
                    job.join()
                }.createCoroutine(cont).resume(Unit)
            }
        }

        return job
    }
}