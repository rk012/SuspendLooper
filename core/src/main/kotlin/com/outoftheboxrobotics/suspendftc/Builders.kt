package com.outoftheboxrobotics.suspendftc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Yields to the [Looper] in the coroutine context, resuming after other scheduled tasks run.
 */
suspend fun yieldLooper() = suspendCancellableCoroutine {
    requireNotNull(it.context[Looper.LooperContext]?.looper) { "No looper found in coroutine context" }.addTask {
        it.resume(Unit)
    }
}

/**
 * While loop that calls [yieldLooper] after each iteration.
 */
suspend inline fun loopYieldWhile(predicate: () -> Boolean, block: () -> Unit) {
    while (predicate()) {
        block()
        yieldLooper()
    }
}

/**
 * Repeatedly calls [loopYieldWhile] until condition is satisfied.
 */
suspend inline fun suspendUntil(predicate: () -> Boolean) = loopYieldWhile({ !predicate() }) {}

/**
 * Suspends for time delay. The actual time delay is dependent on the event loop frequency, but is guaranteed to be at
 * least the specified amount.
 */
suspend fun suspendFor(millis: Long) {
    val time = System.currentTimeMillis()
    suspendUntil { System.currentTimeMillis() - time >= millis }
}

/**
 * Runs the specified block on the given looper, suspending until completion.
 */
suspend fun <T> withLooper(looper: Looper, block: suspend CoroutineScope.() -> T) = withContext(looper.context) {
    yieldLooper()
    block()
}.also { yieldLooper() }
