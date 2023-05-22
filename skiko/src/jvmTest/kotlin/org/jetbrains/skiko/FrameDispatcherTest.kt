package org.jetbrains.skiko

import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.Executors

@OptIn(ExperimentalCoroutinesApi::class)
class FrameDispatcherTest {
    private var frameCount = 0

    @Test
    fun `test both barriers`() = test {
        val vsyncBarrier = VsyncBarrier()
        val overCommitmentBarrier = OverCommitmentBarrier()

        vsyncBarrier.enable()

        var maxInflightCommands = 0
        var inflightCommands = 0
        var framesRendered = 0

        val frameDispatcher = FrameDispatcher(scope = this, vsyncBarrier, overCommitmentBarrier) {
            inflightCommands += 1
            maxInflightCommands = maxOf(maxInflightCommands, inflightCommands)

            framesRendered += 1

            launch {
                delay(40)
                inflightCommands -= 1
                overCommitmentBarrier.signal()
            }
        }

        val vsyncsCount = 50

        launch {
            repeat(vsyncsCount) {
                vsyncBarrier.signal()
                delay(20)
            }
        }

        launch {
            repeat(500) {
                frameDispatcher.scheduleFrame()
                frameDispatcher.scheduleFrame()

                delay(5)
            }
        }

        advanceUntilIdle()

        assertTrue(framesRendered <= vsyncsCount)
        assertTrue(maxInflightCommands <= OverCommitmentBarrier.MAX_INFLIGHT_COMMAND_BUFFERS)
    }

    @Test
    fun `test vsync barrier`() = test {
        val vsyncBarrier = VsyncBarrier()

        vsyncBarrier.enable()

        var framesRendered = 0

        val frameDispatcher = FrameDispatcher(scope = this, vsyncBarrier) {
            framesRendered += 1
        }

        val vsyncsCount = 50

        launch {
            repeat(vsyncsCount) {
                vsyncBarrier.signal()
                delay(20)
            }
        }

        launch {
            repeat(500) {
                frameDispatcher.scheduleFrame()
                frameDispatcher.scheduleFrame()
                frameDispatcher.scheduleFrame()
                frameDispatcher.scheduleFrame()


                delay(5)
            }
        }

        advanceUntilIdle()

        assertTrue(framesRendered <= vsyncsCount)
    }

    @Test
    fun `shouldn't call onFrame after the creating`() = test {
        FrameDispatcher(scope = this) {
            frameCount++
        }

        advanceUntilIdle()

        assertEquals(0, frameCount)
    }

    @Test
    fun `scheduleFrame after creating`() = test {
        val frameDispatcher = FrameDispatcher(scope = this) {
            frameCount++
        }

        frameDispatcher.scheduleFrame()
        advanceUntilIdle()

        assertEquals(1, frameCount)
    }

    @Test
    fun `scheduleFrame multiple times after creating`() = test {
        val frameDispatcher = FrameDispatcher(scope = this) {
            frameCount++
        }

        frameDispatcher.scheduleFrame()
        frameDispatcher.scheduleFrame()
        frameDispatcher.scheduleFrame()
        frameDispatcher.scheduleFrame()
        advanceUntilIdle()

        assertEquals(1, frameCount)
    }

    @Test
    fun `scheduleFrame second time after first onFrame`() = test {
        val frameDispatcher = FrameDispatcher(scope = this) {
            frameCount++
        }

        frameDispatcher.scheduleFrame()
        advanceUntilIdle()

        frameDispatcher.scheduleFrame()
        advanceUntilIdle()
        assertEquals(2, frameCount)
    }

    @Test
    fun `scheduleFrame second time twice after first onFrame`() = test {
        val frameDispatcher = FrameDispatcher(scope = this) {
            frameCount++
        }

        frameDispatcher.scheduleFrame()
        advanceUntilIdle()

        frameDispatcher.scheduleFrame()
        frameDispatcher.scheduleFrame()
        advanceUntilIdle()
        assertEquals(2, frameCount)
    }

    @Test
    fun `scheduleFrame during onFrame`() = test {
        lateinit var frameDispatcher: FrameDispatcher
        frameDispatcher = FrameDispatcher(scope = this) {
            frameCount++
            frameDispatcher.scheduleFrame()
        }
        frameDispatcher.scheduleFrame()

        yield()
        assertEquals(1, frameCount)

        yield()
        assertEquals(2, frameCount)

        yield()
        assertEquals(3, frameCount)

        yield()
        assertEquals(4, frameCount)
    }

    @Test
    fun `scheduleFrame multiple times during onFrame`() = test {
        lateinit var frameDispatcher: FrameDispatcher
        frameDispatcher = FrameDispatcher(scope = this) {
            frameCount++
            frameDispatcher.scheduleFrame()
            frameDispatcher.scheduleFrame()
            frameDispatcher.scheduleFrame()
        }
        frameDispatcher.scheduleFrame()

        yield()
        assertEquals(1, frameCount)

        yield()
        assertEquals(2, frameCount)

        yield()
        assertEquals(3, frameCount)

        yield()
        assertEquals(4, frameCount)
    }

    @Test
    fun `cancel coroutine scope`() = test {
        val scope = CoroutineScope(coroutineContext)

        lateinit var frameDispatcher: FrameDispatcher
        frameDispatcher = FrameDispatcher(scope) {
            frameCount++
            frameDispatcher.scheduleFrame()
        }
        frameDispatcher.scheduleFrame()

        yield()
        assertEquals(1, frameCount)

        yield()
        assertEquals(2, frameCount)

        yield()
        assertEquals(3, frameCount)

        scope.cancel()
        yield()
        assertEquals(3, frameCount)

        advanceUntilIdle()
        assertEquals(3, frameCount)
    }

    @Test
    fun `perform tasks scheduled in the frame after the frame`() {
        val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        val history = mutableListOf<String>()

        runBlocking(dispatcher) {
            val job = launch {
                val scope = this

                lateinit var frameDispatcher: FrameDispatcher
                frameDispatcher = FrameDispatcher(scope = scope) {
                    history.add("frame$frameCount")
                    if (frameCount == 0) {
                        scope.launch {
                            history.add("task")
                        }
                        frameDispatcher.scheduleFrame()
                    }
                    frameCount++
                }
                frameDispatcher.scheduleFrame()
            }

            repeat(20) {
                yield()
            }

            job.cancel()
        }

        assertEquals(listOf("frame0", "task", "frame1"), history)
    }

    private fun test(
        block: suspend TestCoroutineScope.() -> Unit
    ) = runBlockingTest {
        pauseDispatcher()
        val job = Job()
        TestCoroutineScope(coroutineContext + job).block()
        job.cancel()
    }
}