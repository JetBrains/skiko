package org.jetbrains.skiko.swing

import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

class DisplayClockTest {

    @Test
    fun `a throwing listener is dropped after one exception`() {
        val clock = ManualClock()
        val throwCount = AtomicInteger()
        val siblingTicks = AtomicInteger()

        clock.add { _, _ ->
            throwCount.incrementAndGet()
            error("deliberate test exception")
        }
        clock.add { _, _ -> siblingTicks.incrementAndGet() }

        clock.tick()
        clock.tick()
        clock.tick()

        assertEquals(1, throwCount.get(), "throwing listener should be dropped after the first exception")
        assertEquals(3, siblingTicks.get(), "sibling should keep receiving every tick")
    }

    @Test
    fun `a lone throwing listener does not break later ticks`() {
        val clock = ManualClock()
        val throwCount = AtomicInteger()
        clock.add { _, _ ->
            throwCount.incrementAndGet()
            error("deliberate test exception")
        }

        clock.tick()
        clock.tick()

        assertEquals(1, throwCount.get())
        assertEquals(0, clock.stopCount, "dropping a listener must not tear the clock down")
    }

    @Test
    fun `closing a healthy sibling before the dropped listener stops the clock once`() {
        val clock = ManualClock()
        val throwing: (Long, Long) -> Unit = { _, _ -> error("deliberate test exception") }
        val sibling: (Long, Long) -> Unit = { _, _ -> }

        clock.add(throwing)
        clock.add(sibling)
        clock.tick()

        assertTrue(clock.remove(sibling))
        assertEquals(1, clock.stopCount)
        assertFalse(clock.remove(throwing), "closing the already-dropped listener must not stop again")
        assertEquals(1, clock.stopCount)
    }

    @Test
    fun `two throwing listeners then both closes stop the clock once`() {
        val clock = ManualClock()
        val first: (Long, Long) -> Unit = { _, _ -> error("first") }
        val second: (Long, Long) -> Unit = { _, _ -> error("second") }

        clock.add(first)
        clock.add(second)
        clock.tick()

        assertTrue(clock.remove(first))
        assertEquals(1, clock.stopCount)
        assertFalse(clock.remove(second))
        assertEquals(1, clock.stopCount)
    }

    private class ManualClock : DisplayClock(displayId = 1L, periodNanos = 16_666_666L) {
        var stopCount = 0
            private set

        override fun onStart() = Unit
        override fun onStop() {
            stopCount++
        }

        fun tick(timeNanos: Long = 0L) = deliver(timeNanos)
    }
}
