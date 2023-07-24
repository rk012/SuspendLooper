package com.outoftheboxrobotics.suspendftc

import kotlinx.coroutines.*
import kotlin.test.Test
import kotlin.test.assertEquals

class MultiLooperTest {
    @Test
    fun contextSwitchingTest() {
        val l1 = Looper()
        val l2 = Looper()

        val main = Thread.currentThread()
        var side: Thread? = null

        // Should run on same thread as loopers.
        l1.scheduleCoroutine {
            assertEquals(main, Thread.currentThread())
            withLooper(l2) {
                assertEquals(side, Thread.currentThread())
            }
            assertEquals(main, Thread.currentThread())

            l1.cancel()
            l2.cancel()
        }

        runBlocking {
            withTimeout(50) {
                launch(Dispatchers.Default) {
                    side = Thread.currentThread()
                    check(main != side)
                    l2.run()
                }

                l1.run()
            }
        }
    }

    @Test
    fun looperSwitchTest() {
        val l1 = Looper()
        val l2 = Looper()

        val deferred = CompletableDeferred<Unit>()

        val main = Thread.currentThread()
        var side: Thread? = null

        // Unconfined dispatcher should run on different thread until looper yield.
        l1.scheduleCoroutine {
            assertEquals(main, Thread.currentThread())
            deferred.await()
            assertEquals(side, Thread.currentThread())
            yieldLooper()
            assertEquals(main, Thread.currentThread())
            l1.cancel()
            l2.cancel()
        }

        l2.scheduleCoroutine {
            deferred.complete(Unit)
        }

        runBlocking {
            withTimeout(100) {
                launch(Dispatchers.Default) {
                    side = Thread.currentThread()
                    check(main != side)
                    delay(50)
                    l2.run()
                }

                l1.run()
            }
        }
    }
}