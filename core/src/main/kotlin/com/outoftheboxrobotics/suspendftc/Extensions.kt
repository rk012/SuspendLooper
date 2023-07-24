package com.outoftheboxrobotics.suspendftc

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Locks the Mutex and yields to the current [Looper].
 */
suspend fun Mutex.lockAndYield(owner: Any? = null) {
    lock(owner)
    yieldLooper()
}

/**
 * Acquires Mutex lock and yields to the current [Looper] before running the given action.
 */
suspend inline fun <T> Mutex.withLockAndYield(owner: Any? = null, action: () -> T) = withLock(owner) {
    yieldLooper()
    action()
}

/**
 * Sends item and yields to the current [Looper]
 */
suspend fun <E> SendChannel<E>.sendAndYield(item: E) {
    send(item)
    yieldLooper()
}

/**
 * Receives item and yields to the current [Looper]
 */
suspend fun <E> ReceiveChannel<E>.receiveAndYield() = receive().also { yieldLooper() }

/**
 * Waits for completion and yields to the current [Looper]
 */
suspend fun <T> CompletableDeferred<T>.awaitAndYield(): T = await().also { yieldLooper() }

/**
 * Waits for completion and yields to the current [Looper]
 */
suspend fun Job.joinAndYield() = join().also { yieldLooper() }
