package org.jetbrains.skiko

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.ceil
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
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

    private fun frameLimiterTest(
        frameLimitMillis: Long,
        delayPrecisionMillis: Long,
        block: suspend TestScope.(FrameLimiter) -> Unit
    ) =
        runFrameTest {
            val limiter = FrameLimiter(
                backgroundScope,
                frameMillis = { frameLimitMillis },
                currentTime = { testScheduler.currentTime.milliseconds },
                impreciseDelay = { timeMillis ->
                    val ms = ceil(timeMillis.toDouble() / delayPrecisionMillis).toInt() * delayPrecisionMillis
                    delay(ms)
                }
            )
            block(limiter)
        }


    private fun runFrameTest(block: suspend TestScope.() -> Unit) = runTest {
        block()
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun `cancel scope before awaitNextFrame`() = runTest {
        val scope = CoroutineScope(coroutineContext + Job())
        val frameLimiter = FrameLimiter(
            scope,
            frameMillis = { 10 },
            currentTime = { testScheduler.currentTime.milliseconds })

        scope.cancel()

        assertThrow<kotlin.coroutines.cancellation.CancellationException> {
            frameLimiter.awaitNextFrame()
        }
    }

    @Test
    fun `cancel scope after awaitNextFrame`() = runTest {
        val scope = CoroutineScope(coroutineContext + Job())

        val frameLimiter = FrameLimiter(
            scope,
            frameMillis = { 10 },
            currentTime = { testScheduler.currentTime.milliseconds })

        launch {
            scope.cancel()
        }

        assertThrow<kotlin.coroutines.cancellation.CancellationException> {
            frameLimiter.awaitNextFrame()
        }
    }

    private inline fun <reified T : Throwable> assertThrow(body: () -> Unit) {
        var actualE: Throwable? = null
        try {
            body()
        } catch (e: Throwable) {
            actualE = e
        }
        assertTrue("Actual ${actualE?.javaClass}, expected ${T::class.java}", actualE is T)
    }

    @Test(timeout = 30000)
    fun `multithreaded awaiter`() {
        val scope = CoroutineScope(Dispatchers.IO)
        val frameLimiter = FrameLimiter(scope, { 0 })

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
        val frameLimiter = FrameLimiter(scope, { 0 })

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
}