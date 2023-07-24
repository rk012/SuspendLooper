package com.outoftheboxrobotics.suspendftc

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.DurationUnit
import kotlin.time.measureTime

class BuilderTest {
    @Test
    fun yieldTest() {
        var str = ""
        val looper = Looper()

        looper.scheduleCoroutine {
            coroutineScope {
                str += 'A'
                yieldLooper()
                str += 'B'

                launch {
                    // Order is for 'C's are implementation specific, but both must occur within the same loop cycle.
                    str += 'C'
                    yieldLooper()
                    str += 'D'
                }

                str += 'C'
            }

            str += 'E'
            looper.cancel()
        }

        runLooper(looper)

        assertEquals("ABCCDE", str)
    }

    @Test
    fun loopUntilTest() {
        val looper = Looper()

        var flag = false

        looper.scheduleCoroutine {
            launch {
                assertFalse(flag)
                suspendFor(10)
                flag = true
                yieldLooper()
                assertFalse(flag)
                looper.cancel()
            }

            suspendUntil { flag }
            flag = false
        }

        runLooper(looper)
    }

    @Test
    fun yieldDelayTest() {
        val looper = Looper()

        looper.scheduleCoroutine {
            suspendFor(100)
            looper.cancel()
        }

        runBlocking {
            val time = measureTime {
                withTimeout(200) { looper.run() }
            }

            assertEquals(100.0, time.toDouble(DurationUnit.MILLISECONDS), 50.0)
        }
    }
}