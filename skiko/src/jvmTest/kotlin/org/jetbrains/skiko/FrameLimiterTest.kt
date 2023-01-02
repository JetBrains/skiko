package org.jetbrains.skiko

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Ignore
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.math.ceil
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@Ignore // Leaving for Igor to fix (sorry!)
class FrameLimiterTest {
    private val frameCount = 8
    private val frames = 0 until frameCount
    
    @Test
    fun `limit 10ms, render 0ms`() {
        fun frameTicksOf(delayPrecisionMillis: Long) =
            frameTicksOf(frameLimitMillis = 10, delayPrecisionMillis, frameRenderMillis = 0)

        assertEquals(frames.map { it * 10 }, frameTicksOf(delayPrecisionMillis = 1))
        assertEquals(frames.map { it * 9 }, frameTicksOf(delayPrecisionMillis = 3))
        assertEquals(frames.map { it * 7 }, frameTicksOf(delayPrecisionMillis = 7))
        assertEquals(frames.map { it * 10 }, frameTicksOf(delayPrecisionMillis = 10))
        assertEquals(frames.map { it * 11 }, frameTicksOf(delayPrecisionMillis = 11))
        assertEquals(frames.map { it * 21 }, frameTicksOf(delayPrecisionMillis = 21))
    }

    @Test
    fun `limit 10ms, render 1ms`() {
        fun frameTicksOf(delayPrecisionMillis: Long) =
            frameTicksOf(frameLimitMillis = 10, delayPrecisionMillis, frameRenderMillis = 1)

        assertEquals(frames.map { it * 10 }, frameTicksOf(delayPrecisionMillis = 1))
        assertEquals(frames.map { it * 9 }, frameTicksOf(delayPrecisionMillis = 3))
        assertEquals(frames.map { it * 7 }, frameTicksOf(delayPrecisionMillis = 7))
        assertEquals(frames.map { it * 10 }, frameTicksOf(delayPrecisionMillis = 10))
        assertEquals(frames.map { it * 11 }, frameTicksOf(delayPrecisionMillis = 11))
        assertEquals(frames.map { it * 21 }, frameTicksOf(delayPrecisionMillis = 21))
    }

    @Test
    fun `limit 10ms, render 9ms`() {
        fun frameTicksOf(delayPrecisionMillis: Long) =
            frameTicksOf(frameLimitMillis = 10, delayPrecisionMillis, frameRenderMillis = 9)

        assertEquals(frames.map { it * 10 }, frameTicksOf(delayPrecisionMillis = 1))
        assertEquals(frames.map { it * 9 }, frameTicksOf(delayPrecisionMillis = 3))
        assertEquals(frames.map { it * 9 }, frameTicksOf(delayPrecisionMillis = 7))
        assertEquals(frames.map { it * 10 }, frameTicksOf(delayPrecisionMillis = 10))
        assertEquals(frames.map { it * 11 }, frameTicksOf(delayPrecisionMillis = 11))
        assertEquals(frames.map { it * 21 }, frameTicksOf(delayPrecisionMillis = 21))
    }

    @Test
    fun `limit 10ms, render 10ms`() {
        fun frameTicksOf(delayPrecisionMillis: Long) =
            frameTicksOf(frameLimitMillis = 10, delayPrecisionMillis, frameRenderMillis = 10)

        assertEquals(frames.map { it * 10 }, frameTicksOf(delayPrecisionMillis = 1))
        assertEquals(frames.map { it * 10 }, frameTicksOf(delayPrecisionMillis = 3))
        assertEquals(frames.map { it * 10 }, frameTicksOf(delayPrecisionMillis = 7))
        assertEquals(frames.map { it * 10 }, frameTicksOf(delayPrecisionMillis = 10))
        assertEquals(frames.map { it * 11 }, frameTicksOf(delayPrecisionMillis = 11))
        assertEquals(frames.map { it * 21 }, frameTicksOf(delayPrecisionMillis = 21))
    }

    @Test
    fun `limit 10ms, render 11ms`() {
        fun frameTicksOf(delayPrecisionMillis: Long) =
            frameTicksOf(frameLimitMillis = 10, delayPrecisionMillis, frameRenderMillis = 11)

        assertEquals(frames.map { it * 11 }, frameTicksOf(delayPrecisionMillis = 1))
        assertEquals(frames.map { it * 11 }, frameTicksOf(delayPrecisionMillis = 3))
        assertEquals(frames.map { it * 11 }, frameTicksOf(delayPrecisionMillis = 7))
        assertEquals(frames.map { it * 11 }, frameTicksOf(delayPrecisionMillis = 10))
        assertEquals(frames.map { it * 11 }, frameTicksOf(delayPrecisionMillis = 11))
        assertEquals(frames.map { it * 21 }, frameTicksOf(delayPrecisionMillis = 21))
    }

    @Test
    fun `limit 10ms, render 21ms`() {
        fun frameTicksOf(delayPrecisionMillis: Long) =
            frameTicksOf(frameLimitMillis = 10, delayPrecisionMillis, frameRenderMillis = 21)

        assertEquals(frames.map { it * 21 }, frameTicksOf(delayPrecisionMillis = 1))
        assertEquals(frames.map { it * 21 }, frameTicksOf(delayPrecisionMillis = 3))
        assertEquals(frames.map { it * 21 }, frameTicksOf(delayPrecisionMillis = 7))
        assertEquals(frames.map { it * 21 }, frameTicksOf(delayPrecisionMillis = 10))
        assertEquals(frames.map { it * 21 }, frameTicksOf(delayPrecisionMillis = 11))
        assertEquals(frames.map { it * 21 }, frameTicksOf(delayPrecisionMillis = 21))
    }

    @Suppress("SameParameterValue")
    private fun frameTicksOf(
        frameLimitMillis: Long,
        delayPrecisionMillis: Long,
        frameRenderMillis: Long,
    ): List<Int> {
        val ticks = mutableListOf<Int>()
        frameLimiterTest(
            frameLimitMillis,
            delayPrecisionMillis
        ) { limiter ->
            repeat(frameCount) {
                limiter.awaitNextFrame()
                ticks.add(currentTime.toInt())
                advanceTimeBy(frameRenderMillis)
            }
        }
        return ticks
    }

    @Test
    fun `multiple awaiters`() {
        val ticks1 = mutableListOf<Int>()
        val ticks2 = mutableListOf<Int>()
        val ticks3 = mutableListOf<Int>()
        frameLimiterTest(
            frameLimitMillis = 10,
            delayPrecisionMillis = 1
        ) { limiter ->
            launch {
                repeat(frameCount) {
                    limiter.awaitNextFrame()
                    ticks1.add(currentTime.toInt())
                    advanceTimeBy(3)
                }
            }
            launch {
                repeat(frameCount) {
                    limiter.awaitNextFrame()
                    ticks2.add(currentTime.toInt())
                    advanceTimeBy(1)
                }
            }
            launch {
                repeat(frameCount) {
                    limiter.awaitNextFrame()
                    ticks3.add(currentTime.toInt())
                    advanceTimeBy(1)
                }
            }
        }

        assertEquals(frames.map { it * 10 }, ticks1)
        assertEquals(frames.map { it * 10 }, ticks2)
        assertEquals(frames.map { it * 10 }, ticks3)
    }

    @Test
    fun `cancel scope before awaitNextFrame`() = runBlockingTest {
        pauseDispatcher()
        val scope = CoroutineScope(coroutineContext + Job())
        val frameLimiter = FrameLimiter(scope, { 10 }, nanoTime = { currentTime.milliseconds.inWholeNanoseconds })

        scope.cancel()
        assertFailsWith(CancellationException::class) {
            frameLimiter.awaitNextFrame()
        }
    }

    @Test
    fun `cancel scope after awaitNextFrame`() = runBlockingTest {
        pauseDispatcher()
        val scope = CoroutineScope(coroutineContext + Job())

        val frameLimiter = FrameLimiter(scope, { 10 }, nanoTime = { currentTime.milliseconds.inWholeNanoseconds })

        launch {
            scope.cancel()
        }

        assertFailsWith(CancellationException::class) {
            frameLimiter.awaitNextFrame()
        }
    }

    @Test(timeout = 30000)
    fun `multithreaded awaiter`() {
        val scope = CoroutineScope(Dispatchers.IO)
        val frameLimiter = FrameLimiter(scope, { 0 }, nanoTime = System::nanoTime)

        runBlocking(Dispatchers.IO) {
            repeat(50000) {
                frameLimiter.awaitNextFrame()
            }
        }

        scope.cancel()
    }

    @Test(timeout = 30000)
    fun `multiple multithreaded awaiters`() {
        val scope = CoroutineScope(Dispatchers.IO)
        val frameLimiter = FrameLimiter(scope, { 0 }, nanoTime = System::nanoTime)

        runBlocking(Dispatchers.IO) {
            repeat(3) {
                launch {
                    repeat(50000) {
                        frameLimiter.awaitNextFrame()
                        yield()
                        yield()
                        yield()
                    }
                }
            }
        }

        scope.cancel()
    }

    private fun frameLimiterTest(
        frameLimitMillis: Long,
        delayPrecisionMillis: Long,
        block: suspend TestCoroutineScope.(FrameLimiter) -> Unit
    ) {
        runBlockingTest(context = CoroutineName("runBlockingTest")) {
            val dispatcher = NonpreciseTestCoroutineDispatcher(
                delayPrecisionMillis = delayPrecisionMillis,
                scheduler = testScheduler
            )
            withContext(dispatcher) {
                val scope = CoroutineScope(coroutineContext + CoroutineName("frameLimiterTest") + Job())
                val limiter = FrameLimiter(
                    scope,
                    frameMillis = { frameLimitMillis },
                    nanoTime = { currentTime.milliseconds.inWholeNanoseconds }
                )
                block(limiter)
                scope.cancel()
            }
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    private class NonpreciseTestCoroutineDispatcher(
        private val delayPrecisionMillis: Long,
        scheduler: TestCoroutineScheduler,
        private val original: TestCoroutineDispatcher = TestCoroutineDispatcher(scheduler)
    ) : CoroutineDispatcher(), Delay {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            original.dispatch(context, block)
        }

        override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
            val delay = ceil(timeMillis.toDouble() / delayPrecisionMillis).toInt() * delayPrecisionMillis
            original.scheduleResumeAfterDelay(delay, continuation)
        }
    }
}
