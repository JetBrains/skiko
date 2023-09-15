package org.jetbrains.skiko

import kotlinx.coroutines.*
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class RendezvousBroadcastChannelTest {
    @Test(timeout = 5000)
    fun `receive, send`() {
        var actualValue = -1

        val channel = RendezvousBroadcastChannel<Int>()

        runBlocking {
            launch {
                actualValue = channel.receive()
            }

            launch {
                channel.sendAll(1)
            }
        }

        assertEquals(1, actualValue)
    }

    @Test(timeout = 5000)
    fun `send, receive`() {
        var actualValue = -1

        val channel = RendezvousBroadcastChannel<Int>()

        runBlocking {
            launch {
                channel.sendAll(1)
            }

            launch {
                actualValue = channel.receive()
            }
        }

        assertEquals(1, actualValue)
    }

    @Test(timeout = 5000)
    fun `send when there is multiple receivers`() {
        val actualValues = mutableListOf<Int>()

        val channel = RendezvousBroadcastChannel<Int>()

        runBlocking {
            repeat(5) {
                launch {
                    actualValues.add(channel.receive())
                }
            }

            launch {
                channel.sendAll(1)
            }
        }

        assertEquals(listOf(1, 1, 1, 1, 1), actualValues)
    }

    @Test(timeout = 30000)
    fun `multithreading sending and receiving should not cause deadlock`() {
        val channel = RendezvousBroadcastChannel<Int>()

        runBlocking {
            val receiverJobs = (1..10).map {
                launch(Dispatchers.IO) {
                    repeat(1000) {
                        channel.receive()
                    }
                }
            }

            val sendingJob = launch(Dispatchers.IO) {
                while (true) {
                    channel.sendAll(1)
                }
            }

            receiverJobs.joinAll()
            sendingJob.cancel()
        }
    }

    @Test
    fun `first send should not end if there is no received value`() {
        var isExceptionThrown = false
        val channel = RendezvousBroadcastChannel<Int>()
        runBlocking {
            try {
                withTimeout(1000) {
                    channel.sendAll(1)
                }
            } catch (e: TimeoutCancellationException) {
                isExceptionThrown = true
            }
        }

        assertEquals(true, isExceptionThrown)
    }

    @Test
    fun `second send should not end if there is no second received value`() {
        var isExceptionThrown = false
        val channel = RendezvousBroadcastChannel<Int>()
        runBlocking {
            launch {
                channel.sendAll(1)
                try {
                    withTimeout(1000) {
                        channel.sendAll(1)
                    }
                } catch (e: TimeoutCancellationException) {
                    isExceptionThrown = true
                }
            }

            launch {
                channel.receive()
            }
        }

        assertEquals(true, isExceptionThrown)
    }

    @Test
    fun `first receive should not end if there is no received value`() {
        var isExceptionThrown = false
        val channel = RendezvousBroadcastChannel<Int>()
        runBlocking {
            try {
                withTimeout(1000) {
                    channel.receive()
                }
            } catch (e: TimeoutCancellationException) {
                isExceptionThrown = true
            }
        }

        assertEquals(true, isExceptionThrown)
    }

    @Test
    fun `second receive should not end if there is no second received value`() {
        var isExceptionThrown = false
        val channel = RendezvousBroadcastChannel<Int>()
        runBlocking {
            launch {
                channel.receive()
                try {
                    withTimeout(1000) {
                        channel.receive()
                    }
                } catch (e: TimeoutCancellationException) {
                    isExceptionThrown = true
                }
            }

            launch {
                channel.sendAll(1)
            }
        }

        assertEquals(true, isExceptionThrown)
    }

    @Test
    fun `produce values, consume from multiple coroutines`() = runTest {

        val frames1 = mutableListOf<Int>()
        val frames2 = mutableListOf<Int>()
        val frames3 = mutableListOf<Int>()

        val channel = RendezvousBroadcastChannel<Int>()

        val produceJob = launch {
            for (i in 0 until 1000) {
                channel.sendAll(i)
                delay(10)
            }
        }

        val random = Random(5435)

        launch {
            repeat(1000) {
                frames1.add(channel.receive())
                delay(random.nextLong(10L))
            }
        }
        launch {
            repeat(1000) {
                frames2.add(channel.receive())
                delay(random.nextLong(10L))
            }
        }
        launch {
            repeat(1000) {
                frames3.add(channel.receive())
                delay(random.nextLong(10L))
            }
        }

        testScheduler.advanceUntilIdle()
        produceJob.cancel()

        assertEquals((0 until 1000).toList(), frames1)
        assertEquals((0 until 1000).toList(), frames2)
        assertEquals((0 until 1000).toList(), frames3)
    }
}