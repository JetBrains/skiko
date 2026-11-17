package org.jetbrains.skiko.swing

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

/**
 * The loop every native clock runs, driven with fake sources so its contract is checked on every
 * platform and not only where the hardware happens to misbehave.
 */
class NativeVBlankClockTest {

    /** Completes every wait at once, like `WaitForVBlank` on a display in power save. */
    private class SpinningSource(periodNanos: Long) : NativeVBlankClock(DISPLAY, periodNanos) {
        val closed = CountDownLatch(1)
        override fun open(): Long = 1
        override fun waitTick(handle: Long): Long = System.nanoTime()
        override fun close(handle: Long) = closed.countDown()
    }

    /** Ticks once, then reports the source gone. */
    private class DyingSource : NativeVBlankClock(DISPLAY, 10_000_000L) {
        val closed = CountDownLatch(1)
        private var calls = 0
        override fun open(): Long = 1
        override fun waitTick(handle: Long): Long = if (calls++ == 0) System.nanoTime() else -1
        override fun close(handle: Long) = closed.countDown()
    }

    private class UnopenableSource : NativeVBlankClock(DISPLAY, 10_000_000L) {
        var closeCalls = 0
        override fun open(): Long = 0
        override fun waitTick(handle: Long): Long = error("must not be called without a source")
        override fun close(handle: Long) { closeCalls++ }
    }

    @Test
    fun `a source that completes every wait at once is held to the nominal period`() {
        val clock = SpinningSource(periodNanos = 20_000_000L) // 50 Hz
        val ticks = AtomicInteger()
        val listener: (Long, Long) -> Unit = { _, _ -> ticks.incrementAndGet() }

        clock.add(listener)
        Thread.sleep(400)
        val delivered = ticks.get()
        assertTrue(clock.remove(listener))

        // Unguarded, this spins at millions of ticks a second. Guarded, it is paced to the nominal
        // period: about 20 in 400 ms. Only the upper bound separates the two; a starved CI runner
        // delivers fewer, never more.
        assertTrue(delivered in 1..60, "expected a nominal-rate cadence, got $delivered ticks in 400 ms")
        // Stopping the clock ends the thread, which closes the source itself.
        assertTrue(clock.closed.await(2, TimeUnit.SECONDS), "source not closed after the clock stopped")
    }

    @Test
    fun `a source that fails ends the clock and closes itself`() {
        val clock = DyingSource()
        val ticks = AtomicInteger()
        val listener: (Long, Long) -> Unit = { _, _ -> ticks.incrementAndGet() }

        clock.add(listener)
        assertTrue(clock.closed.await(2, TimeUnit.SECONDS), "source not closed after it failed")
        assertEquals(1, ticks.get(), "the tick before the failure is delivered, nothing after")
        clock.remove(listener)
    }

    /** Ticks at a fixed interval like a real display, whatever period the clock was told. */
    private class PacedSource(intervalNanos: Long, periodNanos: Long) : NativeVBlankClock(DISPLAY, periodNanos) {
        private val intervalMillis = intervalNanos / 1_000_000
        val produced = AtomicInteger()
        val closed = CountDownLatch(1)
        override fun open(): Long = 1
        override fun waitTick(handle: Long): Long {
            try {
                Thread.sleep(intervalMillis)
            } catch (_: InterruptedException) {
                return -1 // An interrupted wait fails, as a real source's would.
            }
            produced.incrementAndGet()
            return System.nanoTime()
        }
        override fun close(handle: Long) = closed.countDown()
    }

    @Test
    fun `a source with no known period is not throttled at any plausible rate`() {
        // 240 Hz from a clock that was told nothing about its display: every tick goes through.
        val clock = PacedSource(intervalNanos = 4_000_000L, periodNanos = 0)
        val ticks = AtomicInteger()
        val listener: (Long, Long) -> Unit = { _, _ -> ticks.incrementAndGet() }

        clock.add(listener)
        Thread.sleep(200)
        val produced = clock.produced.get()
        val delivered = ticks.get()
        clock.remove(listener)

        // Compared with what the source produced, not with the wall clock: a slow CI runner
        // stretches the sleeps, but a tick that was produced is either delivered or throttled.
        assertTrue(produced > 0, "the source never ticked")
        assertTrue(delivered >= produced - 1, "a plausible rate was throttled: $delivered of $produced ticks")
        assertTrue(clock.closed.await(2, TimeUnit.SECONDS))
    }

    @Test
    fun `a source with no known period is still held back when it spins`() {
        // Immediate completions are faster than any display, so with no period to hold to, 60 Hz
        // stands in — a stand-in for the impossible case only, never for a healthy display.
        val clock = SpinningSource(periodNanos = 0)
        val ticks = AtomicInteger()
        val listener: (Long, Long) -> Unit = { _, _ -> ticks.incrementAndGet() }

        clock.add(listener)
        Thread.sleep(400)
        val delivered = ticks.get()
        clock.remove(listener)

        // About 24 in 400 ms; as above, only the upper bound tells a held clock from a spinning one.
        assertTrue(delivered in 1..60, "expected the 60 Hz fallback, got $delivered ticks in 400 ms")
        assertTrue(clock.closed.await(2, TimeUnit.SECONDS))
    }

    @Test
    fun `an interrupt ends a native clock instead of spinning it`() {
        // parkNanos returns at once while the interrupt flag is set; the hold would become a spin.
        val clock = SpinningSource(periodNanos = 20_000_000L)
        awaitNoClockThread()
        val listener: (Long, Long) -> Unit = { _, _ -> }
        clock.add(listener)
        clockThread().interrupt()

        assertTrue(clock.closed.await(2, TimeUnit.SECONDS), "interrupted clock thread did not exit")
        clock.remove(listener)
    }

    @Test
    fun `an interrupt ends the timer clock instead of spinning it`() {
        val clock = TimerClock(DISPLAY, 20_000_000L)
        awaitNoClockThread()
        val listener: (Long, Long) -> Unit = { _, _ -> }
        clock.add(listener)
        val thread = clockThread()
        thread.interrupt()
        thread.join(2_000)

        assertTrue(!thread.isAlive, "interrupted timer thread did not exit")
        clock.remove(listener)
    }

    /**
     * Every clock here shares one display id, so one thread name. A clock stopped by the previous
     * test may still be winding down; interrupting that one would leave this test's clock running.
     */
    private fun awaitNoClockThread() {
        val deadline = System.currentTimeMillis() + 2_000
        while (Thread.getAllStackTraces().keys.any { it.name == "Skiko-FramePacing-$DISPLAY" }) {
            check(System.currentTimeMillis() < deadline) { "a clock thread from an earlier test is still alive" }
            Thread.sleep(5)
        }
    }

    /** The clock's own thread, which [DisplayClock.add] has just started. */
    private fun clockThread(): Thread {
        val deadline = System.currentTimeMillis() + 2_000
        while (System.currentTimeMillis() < deadline) {
            Thread.getAllStackTraces().keys.firstOrNull { it.name == "Skiko-FramePacing-$DISPLAY" }?.let { return it }
            Thread.sleep(5)
        }
        error("clock thread never appeared")
    }

    @Test
    fun `a source that cannot be opened leaves nothing behind`() {
        val clock = UnopenableSource()
        val ticks = AtomicInteger()
        val listener: (Long, Long) -> Unit = { _, _ -> ticks.incrementAndGet() }

        clock.add(listener)
        Thread.sleep(100)
        assertEquals(0, ticks.get())
        assertEquals(0, clock.closeCalls, "nothing was opened, so nothing may be closed")
        assertTrue(
            Thread.getAllStackTraces().keys.none { it.name == "Skiko-FramePacing-$DISPLAY" },
            "clock thread lingering with no source"
        )
        clock.remove(listener)
    }

    private companion object {
        const val DISPLAY = 4242L
    }
}
