package org.jetbrains.skiko

import kotlin.test.*

class WebFrameSchedulerTest {
    @Test
    fun ordinaryModeUsesOnlyRafAndCoalesces() {
        val clock = TestFrameClock()
        var draws = 0
        val scheduler = WebFrameScheduler(clock = clock) { draws++ }
        repeat(5) { scheduler.requestFrame() }
        assertEquals(1, clock.rafs.size)
        assertTrue(clock.timers.isEmpty())
        clock.vsync(16.0)
        assertEquals(1, draws)
        assertFalse(clock.active)
        scheduler.dispose()
        assertTrue(clock.rafs.isEmpty())
    }

    @Test
    fun highRefreshUsesTaskAfterTimerAndStopsWhenIdle() {
        val clock = TestFrameClock()
        var draws = 0
        val scheduler = WebFrameScheduler(120.0, clock) { draws++ }
        repeat(5) { scheduler.requestFrame() }
        assertTrue(clock.active)
        clock.fireTimer()
        assertEquals(0, draws, "timer must yield through MessageChannel")
        clock.fireTask()
        assertEquals(1, draws)
        assertFalse(clock.active)
        assertTrue(clock.rafs.isEmpty())
        assertTrue(clock.timers.isEmpty())
        scheduler.dispose()
        assertTrue(clock.closed)
        assertNull(clock.visibilityListener)
    }

    @Test
    fun rafDefinesPhaseEvenWhenItsRateIsSixtyHz() {
        val clock = TestFrameClock()
        val timestamps = mutableListOf<Double>()
        lateinit var scheduler: WebFrameScheduler
        scheduler = WebFrameScheduler(120.0, clock) { timestamps.add(it); scheduler.requestFrame() }
        scheduler.requestFrame()
        clock.fireTimerAndTask()
        clock.vsync(2.0) // The next requested frame adopts this phase.
        clock.fireTimerAndTask()
        assertEquals(2.0 + 2 * 1000.0 / 120, clock.timers.values.single().first, 0.00001)
        scheduler.dispose()
    }

    @Test
    fun roundedMillisecondsDoNotClampRenderingTo111Hz() {
        val clock = TestFrameClock()
        val timestamps = mutableListOf<Double>()
        lateinit var scheduler: WebFrameScheduler
        scheduler = WebFrameScheduler(120.0, clock) { timestamps.add(it); scheduler.requestFrame() }
        scheduler.requestFrame()
        repeat(121) {
            clock.fireTimerAndTask(roundToMilliseconds = true)
            if (it % 2 == 0) clock.vsync(it * 1000.0 / 120)
        }
        assertTrue(timestamps.last() in 990.0..1020.0, "120 intervals should take about one second: ${timestamps.last()}")
        scheduler.dispose()
    }

    @Test
    fun hiddenPagePausesPendingTaskAndResumesWithoutCatchup() {
        val clock = TestFrameClock()
        var draws = 0
        lateinit var scheduler: WebFrameScheduler
        scheduler = WebFrameScheduler(120.0, clock) { draws++; scheduler.requestFrame() }
        scheduler.requestFrame()
        clock.fireTimer()
        clock.changeVisibility(false)
        assertTrue(clock.tasks.isEmpty())
        assertTrue(clock.rafs.isEmpty())
        assertFalse(clock.active)
        clock.time = 10000.0
        clock.changeVisibility(true)
        clock.fireTimerAndTask()
        assertEquals(1, draws)
        assertTrue(clock.timers.values.single().first > 10000)
        scheduler.dispose()
        assertFalse(clock.active)
        assertTrue(clock.timers.isEmpty())
        scheduler.requestFrame()
        assertTrue(clock.timers.isEmpty())
    }

    @Test
    fun missedDeadlinesDoNotReplayFrames() {
        val clock = TestFrameClock()
        var draws = 0
        lateinit var scheduler: WebFrameScheduler
        scheduler = WebFrameScheduler(120.0, clock) { draws++; scheduler.requestFrame() }
        scheduler.requestFrame()
        clock.fireTimerAndTask()
        clock.fireTimer(at = 5000.0)
        clock.fireTask()
        assertEquals(2, draws)
        assertTrue(clock.timers.values.single().first > 5000)
        scheduler.dispose()
    }
}

private class TestFrameClock : WebFrameClock {
    var time = 0.0
    var active = false
    var closed = false
    private var visible = true
    private var nextId = 0
    var visibilityListener: (() -> Unit)? = null
    val rafs = mutableMapOf<Int, (Double) -> Unit>()
    val timers = mutableMapOf<Int, Pair<Double, () -> Unit>>()
    val tasks = mutableMapOf<Int, () -> Unit>()
    override fun now() = time
    override fun requestAnimationFrame(callback: (Double) -> Unit) = (++nextId).also { rafs[it] = callback }
    override fun cancelAnimationFrame(id: Int) { rafs.remove(id) }
    override fun setTimeout(callback: () -> Unit, delay: Double) = (++nextId).also { timers[it] = time + delay to callback }
    override fun cancelTimeout(id: Int) { timers.remove(id) }
    override fun postTask(callback: () -> Unit) = (++nextId).also { tasks[it] = callback }
    override fun cancelTask(id: Int) { tasks.remove(id) }
    override fun isVisible() = visible
    override fun addVisibilityListener(callback: () -> Unit) { visibilityListener = callback }
    override fun removeVisibilityListener(callback: () -> Unit) { assertSame(visibilityListener, callback); visibilityListener = null }
    override fun setHighRefreshRateActive(active: Boolean) { this.active = active }
    override fun close() { closed = true; tasks.clear() }
    fun changeVisibility(value: Boolean) { visible = value; visibilityListener?.invoke() }
    fun vsync(timestamp: Double) {
        val callbacks = rafs.values.toList(); rafs.clear()
        callbacks.forEach { it(timestamp) }
    }
    fun fireTimer(at: Double? = null) {
        val entry = timers.minBy { it.value.first }
        val timer = entry.value
        timers.remove(entry.key)
        time = at ?: timer.first
        timer.second()
    }
    fun fireTask() {
        val id = tasks.keys.first()
        tasks.remove(id)!!.invoke()
    }
    fun fireTimerAndTask(roundToMilliseconds: Boolean = false) {
        val due = timers.values.minOf { it.first }
        fireTimer(if (roundToMilliseconds) kotlin.math.ceil(due) else due)
        fireTask()
    }
}
