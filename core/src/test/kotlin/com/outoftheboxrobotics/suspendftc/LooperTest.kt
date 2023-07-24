package com.outoftheboxrobotics.suspendftc

import kotlinx.coroutines.*
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal fun runLooper(looper: Looper, timeout: Long = 50) = runBlocking { withTimeout(timeout) { looper.run() } }

class LooperTest {
    @Test
    fun looperTaskTest() {
        var flag = false

        val looper = Looper()

        looper.addTask {
            flag = true
        }

        looper.addTask {
            looper.cancel()
        }

        assertFalse(flag)

        runLooper(looper)

        assertTrue(flag)
    }

    @Test
    fun looperContextTest() {
        val looper = Looper()

        looper.scheduleCoroutine {
            looper.cancel()
            assertEquals(looper, coroutineContext[Looper.LooperContext]?.looper)
        }

        runLooper(looper)
    }

    @Test
    fun looperTimeoutTest() {
        // Should run indefinitely because looper is never cancelled.
        assertThrows<TimeoutCancellationException> { runLooper(Looper()) }
    }

    @Test
    fun threadTest() {
        fun getCurrentThread() = Thread.currentThread()
        val currentThread = getCurrentThread()

        fun assertThread() = assertEquals(currentThread, getCurrentThread())

        val looper = Looper()

        // Should be confined to current thread
        looper.scheduleCoroutine {
            assertThread()

            launch {
                assertThread()
                yield()
                assertThread()
            }

            yield()
            assertThread()
            looper.cancel()
        }

        runLooper(looper)
    }
}