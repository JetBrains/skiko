package org.jetbrains.skiko

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FrameDispatcherTest {
    private var frameCount = 0

    @Test
    fun `shouldn't call onFrame after the creating`() = runTest {
        val frameDispatcher = FrameDispatcher(scope = this) {
            frameCount++
        }

        advanceUntilIdle()

        frameDispatcher.cancel()
        assertEquals(0, frameCount)
    }

    @Test
    fun `scheduleFrame after creating`() = runTest {
        val frameDispatcher = FrameDispatcher(scope = this) {
            frameCount++
        }

        frameDispatcher.scheduleFrame()
        advanceUntilIdle()

        frameDispatcher.cancel()
        assertEquals(1, frameCount)
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
        advanceUntilIdle()

        frameDispatcher.cancel()
        assertEquals(1, frameCount)
    }

    @Test
    fun `scheduleFrame second time after first onFrame`() = runTest {
        val frameDispatcher = FrameDispatcher(scope = this) {
            frameCount++
        }

        frameDispatcher.scheduleFrame()
        advanceUntilIdle()

        frameDispatcher.scheduleFrame()
        advanceUntilIdle()

        frameDispatcher.cancel()
        assertEquals(2, frameCount)
    }

    @Test
    fun `scheduleFrame second time twice after first onFrame`() = runTest {
        val frameDispatcher = FrameDispatcher(scope = this) {
            frameCount++
        }

        frameDispatcher.scheduleFrame()
        advanceUntilIdle()

        frameDispatcher.scheduleFrame()
        frameDispatcher.scheduleFrame()
        advanceUntilIdle()

        frameDispatcher.cancel()
        assertEquals(2, frameCount)
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
        frameDispatcher = FrameDispatcher(coroutineContext) {
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

        frameDispatcher.cancel()
        yield()
        assertEquals(3, frameCount)

        advanceUntilIdle()
        assertEquals(3, frameCount)

    }

    @Test
    fun `perform tasks scheduled in the frame after the frame`() = runTest {
        val history = mutableListOf<String>()

        val job = launch {
            val jobScope = this

            lateinit var frameDispatcher: FrameDispatcher
            frameDispatcher = FrameDispatcher(scope = jobScope) {
                history.add("frame$frameCount")
                if (frameCount == 0) {
                    jobScope.launch {
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

        assertEquals(listOf("frame0", "task", "frame1"), history)
    }
}
