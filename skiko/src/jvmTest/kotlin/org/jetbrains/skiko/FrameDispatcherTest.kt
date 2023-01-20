package org.jetbrains.skiko

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.Executors

class FrameDispatcherTest {
    private var frameCount = 0

    @Test
    fun `shouldn't call onFrame after the creating`() = runTest {
        val frameDispatcher = FrameDispatcher(scope = this) {
            frameCount++
        }

        testScheduler.advanceUntilIdle()

        assertEquals(0, frameCount)
        frameDispatcher.cancel()
    }

    @Test
    fun `scheduleFrame after creating`() = runTest {
        val frameDispatcher = FrameDispatcher(scope = this) {
            frameCount++
        }

        frameDispatcher.scheduleFrame()
        testScheduler.advanceUntilIdle()

        assertEquals(1, frameCount)
        frameDispatcher.cancel()
    }

    @Test
    fun `scheduleFrame multiple times after creating`() = runTest {
        val frameDispatcher = FrameDispatcher(scope = this) {
            frameCount++
        }

        frameDispatcher.scheduleFrame()
        frameDispatcher.scheduleFrame()
        frameDispatcher.scheduleFrame()
        frameDispatcher.scheduleFrame()
        testScheduler.advanceUntilIdle()

        assertEquals(1, frameCount)
        frameDispatcher.cancel()
    }

    @Test
    fun `scheduleFrame second time after first onFrame`() = runTest {
        val frameDispatcher = FrameDispatcher(scope = this) {
            frameCount++
        }

        frameDispatcher.scheduleFrame()
        testScheduler.advanceUntilIdle()

        frameDispatcher.scheduleFrame()
        testScheduler.advanceUntilIdle()
        assertEquals(2, frameCount)
        frameDispatcher.cancel()
    }

    @Test
    fun `scheduleFrame second time twice after first onFrame`() = runTest {
        val frameDispatcher = FrameDispatcher(scope = this) {
            frameCount++
        }

        frameDispatcher.scheduleFrame()
        testScheduler.advanceUntilIdle()

        frameDispatcher.scheduleFrame()
        frameDispatcher.scheduleFrame()
        testScheduler.advanceUntilIdle()
        assertEquals(2, frameCount)
        frameDispatcher.cancel()
    }

    @Test
    fun `scheduleFrame during onFrame`() = runTest {
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
        frameDispatcher.cancel()
    }

    @Test
    fun `scheduleFrame multiple times during onFrame`() = runTest {
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
        frameDispatcher.cancel()
    }

    @Test
    fun `cancel coroutine scope`() = runTest {
        lateinit var frameDispatcher: FrameDispatcher
        val scope = CoroutineScope(coroutineContext + Job())
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

        testScheduler.advanceUntilIdle()
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
}

@OptIn(ExperimentalCoroutinesApi::class)
internal fun TestCoroutineScope.advanceUntilIdle() {
    testScheduler.advanceUntilIdle()
}

@OptIn(ExperimentalCoroutinesApi::class)
internal fun TestCoroutineScope.advanceTimeBy(ms: Long) {
    testScheduler.advanceTimeBy(ms)
}

@OptIn(ExperimentalCoroutinesApi::class)
internal val TestCoroutineScope.currentTime: Long
    get() = testScheduler.currentTime
