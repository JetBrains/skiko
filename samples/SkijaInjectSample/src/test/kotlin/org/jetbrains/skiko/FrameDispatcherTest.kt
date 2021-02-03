package org.jetbrains.skiko

import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FrameDispatcherTest {
    private var frameCount = 0

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

    @Suppress("JoinDeclarationAndAssignment")
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

    @Suppress("JoinDeclarationAndAssignment")
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

    @Suppress("JoinDeclarationAndAssignment")
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

    private fun test(
        block: suspend TestCoroutineScope.() -> Unit
    ) = runBlockingTest {
        pauseDispatcher()
        val job = Job()
        TestCoroutineScope(coroutineContext + job).block()
        job.cancel()
    }
}