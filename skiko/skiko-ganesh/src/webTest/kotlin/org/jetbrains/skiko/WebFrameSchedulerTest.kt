package org.jetbrains.skiko

import kotlin.test.*

class WebFrameSchedulerTest {
    @Test
    fun defaultUsesAnimationFramesAndCoalescesRequests() {
        val clock = TestFrameClock()
        val frames = mutableListOf<Double>()
        val scheduler = WebFrameScheduler(clock = clock) { frames.add(it) }
        repeat(5) { scheduler.requestFrame() }
        assertEquals(1, clock.animationFrames.size)
        assertTrue(clock.timers.isEmpty())
        clock.animationFrame(16.7)
        assertEquals(listOf(16.7), frames)
        scheduler.requestFrame()
        scheduler.dispose()
        assertTrue(clock.animationFrames.isEmpty())
        scheduler.requestFrame()
        assertTrue(clock.animationFrames.isEmpty())
    }

    @Test
    fun timerPreservesFractionalPeriodsAndCanRequestFromCallback() {
        val clock = TestFrameClock()
        val frames = mutableListOf<Double>()
        lateinit var scheduler: WebFrameScheduler
        scheduler = WebFrameScheduler(120.0, clock) {
            frames.add(it)
            scheduler.requestFrame()
        }
        repeat(5) { scheduler.requestFrame() }
        assertEquals(1, clock.timers.size)
        assertTrue(clock.animationFrames.isEmpty())
        repeat(121) { clock.fireNextTimer() }
        assertEquals(121, frames.size)
        assertEquals(1000.0, frames.last(), 0.000001)
        assertEquals(1, clock.timers.size)
        scheduler.dispose()
        assertTrue(clock.timers.isEmpty())
    }

    @Test
    fun earlyTimersWaitAndStallsDoNotReplayMissedFrames() {
        val clock = TestFrameClock()
        val frames = mutableListOf<Double>()
        lateinit var scheduler: WebFrameScheduler
        scheduler = WebFrameScheduler(120.0, clock) {
            frames.add(it)
            scheduler.requestFrame()
        }
        scheduler.requestFrame()
        clock.fireNextTimer()
        clock.fireNextTimer(at = 8.0) // Simulate setTimeout rounding down.
        assertEquals(listOf(0.0), frames)
        assertEquals(1, clock.timers.size)
        clock.fireNextTimer(at = 100.0) // Simulate a busy/suspended worker.
        assertEquals(listOf(0.0, 100.0), frames)
        clock.fireNextTimer()
        assertEquals(100.0 + 1000.0 / 120, frames.last(), 0.000001)
        scheduler.dispose()
    }

    @Test
    fun idleAndDisposedSchedulersDoNotRun() {
        val clock = TestFrameClock()
        var frames = 0
        val scheduler = WebFrameScheduler(120.0, clock) { frames++ }
        assertTrue(clock.timers.isEmpty())
        scheduler.requestFrame()
        scheduler.dispose()
        scheduler.dispose()
        scheduler.requestFrame()
        assertTrue(clock.timers.isEmpty())
        assertEquals(0, frames)
    }

    @Test
    fun invalidRatesAreRejected() {
        for (rate in listOf(0.0, -120.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertFailsWith<IllegalArgumentException> { WebFrameScheduler(rate) {} }
        }
    }
}

private class TestFrameClock : WebFrameClock {
    private var time = 0.0
    private var nextId = 0
    val animationFrames = mutableMapOf<Int, (Double) -> Unit>()
    val timers = mutableMapOf<Int, Pair<Double, () -> Unit>>()

    override fun now() = time
    override fun requestAnimationFrame(callback: (Double) -> Unit): Int =
        (++nextId).also { animationFrames[it] = callback }
    override fun cancelAnimationFrame(id: Int) { animationFrames.remove(id) }
    override fun setTimeout(callback: () -> Unit, delay: Double): Int =
        (++nextId).also { timers[it] = time + delay to callback }
    override fun cancelTimeout(id: Int) { timers.remove(id) }

    fun animationFrame(at: Double) {
        time = at
        val callbacks = animationFrames.values.toList()
        animationFrames.clear()
        callbacks.forEach { it(at) }
    }

    fun fireNextTimer(at: Double? = null) {
        val entry = timers.minBy { it.value.first }
        val timer = entry.value
        timers.remove(entry.key)
        time = at ?: timer.first
        timer.second()
    }
}
