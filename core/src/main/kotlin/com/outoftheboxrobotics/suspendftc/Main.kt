package com.outoftheboxrobotics.suspendftc

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

val mainLooper = Looper()
val sideLooper = Looper()

val channel = Channel<CompletableDeferred<String>>()

val currentThread: String get() = Thread.currentThread().name

@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
fun main() = runBlocking {
    mainLooper.scheduleCoroutine {
        println("Main: Waiting for result on $currentThread")
        val msg = channel.receiveAndYield()
        println("Main: Received message on $currentThread")
        suspendFor(100)
        msg.complete("bozo")
        println("Main: Finished on $currentThread")

        mainLooper.cancel()
    }

    sideLooper.scheduleCoroutine {
        println("Side: Starting on $currentThread")
        suspendFor(100)
        println("Side: Sending message on $currentThread")
        val msg = CompletableDeferred<String>()
        channel.sendAndYield(msg)
        println("Side: Waiting for msg completion on $currentThread")
        val result = msg.awaitAndYield()
        println("Side: Received result '$result' on $currentThread")

        sideLooper.cancel()
    }

    newSingleThreadContext("sideThread").use { sideThread ->
        launch(sideThread.limitedParallelism(1)) {
            sideLooper.run()
        }

        mainLooper.run()
    }
}