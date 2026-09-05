package org.jetbrains.skiko.swing

import org.jetbrains.skiko.SkikoProperties
import java.awt.GraphicsConfiguration
import java.awt.GraphicsEnvironment
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.After
import org.junit.Assume.assumeFalse
import org.junit.Before
import org.junit.Test

class SwingRepaintPacerTest {
    private lateinit var frame: JFrame
    private lateinit var panel: RepaintCountingPanel
    private var pacer: SwingRepaintPacer? = null

    @Before
    fun setUp() {
        assumeFalse(GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance)
        onEdt {
            frame = JFrame()
            panel = RepaintCountingPanel()
        }
    }

    /**
     * Adds the panel to the frame and makes it displayable. Kept out of [setUp] so that tests can
     * exercise the pacer while the panel has no graphics configuration yet (a parentless
     * component).
     */
    private fun attachAndPack() {
        frame.contentPane.add(panel)
        frame.pack()
    }

    @After
    fun tearDown() {
        if (::frame.isInitialized) {
            onEdt {
                pacer?.dispose()
                frame.dispose()
            }
        }
    }

    @Test
    fun passesThroughDirectlyWhenServiceIsAbsent() {
        onEdt {
            pacer = SwingRepaintPacer(panel, service = null)
            attachAndPack()
        }
        onEdt {
            val before = panel.repaintCount.get()
            pacer?.requestRepaint()
            assertEquals(before + 1, panel.repaintCount.get())
        }
    }

    @Test
    fun paintsUnpacedWhileComponentHasNoDisplay() {
        val service = FakeFramePacingService()
        val before = onEdtGet {
            pacer = SwingRepaintPacer(panel, service)
            // A parentless component has no graphics configuration, so there is nothing to
            // subscribe to; the frame loop must repaint without pacing.
            val count = panel.repaintCount.get()
            pacer?.requestRepaint()
            count
        }
        waitUntil { onEdtGet { panel.repaintCount.get() } == before + 1 }
        assertEquals(0, service.subscribeAttempts)
    }

    @Test
    fun paintsFirstFrameImmediatelyAndSubscribesOnce() {
        val service = FakeFramePacingService().apply { periodNanos = HUGE_PERIOD_NANOS }
        onEdt {
            pacer = SwingRepaintPacer(panel, service)
            attachAndPack()
        }
        val baseline = onEdtGet { panel.repaintCount.get() }

        // Multiple requests while the loop is idle coalesce into one immediate repaint...
        onEdt { repeat(3) { pacer?.requestRepaint() } }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 1 }

        // ...after which the loop waits for a tick instead of repainting again.
        settle()
        assertEquals(baseline + 1, onEdtGet { panel.repaintCount.get() })
        assertEquals(1, service.subscribeCount)
        assertEquals(listOf(DISPLAY_A), service.subscribedDisplayIds)
        assertEquals(0, service.closeCount)
    }

    @Test
    fun pacesContinuousRequestsToOneRepaintPerTick() {
        val service = FakeFramePacingService().apply { periodNanos = HUGE_PERIOD_NANOS }
        onEdt {
            pacer = SwingRepaintPacer(panel, service)
            attachAndPack()
        }
        val baseline = onEdtGet { panel.repaintCount.get() }

        // First frame paints immediately, then the loop waits for a tick.
        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 1 }

        // A request while the loop is waiting must not paint before the tick...
        onEdt { pacer?.requestRepaint() }
        settle()
        assertEquals(baseline + 1, onEdtGet { panel.repaintCount.get() })

        // ...and the tick releases exactly one repaint.
        service.tick()
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 2 }

        // A tick with nothing requested releases nothing; it just ends the wait.
        service.tick()
        settle()
        assertEquals(baseline + 2, onEdtGet { panel.repaintCount.get() })

        // The loop is idle again, so the next request paints immediately, without a tick.
        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 3 }
    }

    @Test
    fun paintsUnpacedWhenDisplayCannotBePaced() {
        val service = FakeFramePacingService().apply { refuseSubscriptions = true }
        onEdt {
            pacer = SwingRepaintPacer(panel, service)
            attachAndPack()
        }
        val baseline = onEdtGet { panel.repaintCount.get() }

        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 1 }
        assertEquals(1, service.subscribeAttempts)
        assertEquals(0, service.subscribeCount)

        // The refused display is remembered: later frames must not retry the subscription.
        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 2 }
        assertEquals(1, service.subscribeAttempts)
    }

    @Test
    fun tickTimeoutAfterAClockStallsResubscribesOnTheNextFrame() {
        // The clock ticks once on subscribe and then goes silent, so the wait times out with the
        // subscription having proven itself alive. The tick timeout floors at 50 ms.
        val service = FakeFramePacingService().apply {
            periodNanos = 5_000_000L
            tickOnSubscribe = true
        }
        onEdt {
            pacer = SwingRepaintPacer(panel, service)
            attachAndPack()
        }
        val baseline = onEdtGet { panel.repaintCount.get() }

        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 1 }
        assertEquals(1, service.subscribeCount)

        // The stalled clock's wait times out and drops the subscription...
        waitUntil { onEdtGet { service.closeCount } == 1 }
        // ...without issuing any repaint — the frame was already painted before the wait.
        settle()
        assertEquals(baseline + 1, onEdtGet { panel.repaintCount.get() })

        // A stall is treated as transient, so the next frame re-creates the subscription.
        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 2 }
        waitUntil { onEdtGet { service.subscribeCount } == 2 }
    }

    @Test
    fun doesNotResubscribeToAClockThatNeverTicked() {
        // A backend that accepts the display and then stays silent — a native clock that failed to
        // open its display source looks exactly like this. Re-subscribing on every timeout would
        // rebuild that clock forever and hold the scene to one repaint per timeout.
        val service = FakeFramePacingService().apply { periodNanos = 5_000_000L }
        onEdt {
            pacer = SwingRepaintPacer(panel, service)
            attachAndPack()
        }
        val baseline = onEdtGet { panel.repaintCount.get() }

        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 1 }
        assertEquals(1, service.subscribeCount)

        // No tick ever arrives: the timeout drops the subscription and remembers the display.
        waitUntil { onEdtGet { service.closeCount } == 1 }

        // Later frames paint unpaced and must not attempt the subscription again.
        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 2 }
        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 3 }
        settle()
        assertEquals(1, service.subscribeAttempts)
    }

    @Test
    fun aThrowingBackendDegradesToUnpacedInsteadOfKillingTheFrameLoop() {
        // A missing native entry point raises UnsatisfiedLinkError from subscribe. If that escaped
        // the frame loop it would cancel it, and the layer would stop repainting for good.
        val service = FakeFramePacingService().apply { subscribeThrows = true }
        onEdt {
            pacer = SwingRepaintPacer(panel, service)
            attachAndPack()
        }
        val baseline = onEdtGet { panel.repaintCount.get() }

        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 1 }

        // The frame loop is still alive, and the refused display is not retried.
        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 2 }
        settle()
        assertEquals(1, service.subscribeAttempts)
        assertEquals(0, service.subscribeCount)
    }

    @Test
    fun resubscribesWhenTheDisplayChanges() {
        val service = FakeFramePacingService().apply { periodNanos = HUGE_PERIOD_NANOS }
        onEdt {
            pacer = SwingRepaintPacer(panel, service)
            attachAndPack()
        }
        val baseline = onEdtGet { panel.repaintCount.get() }

        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 1 }
        assertEquals(listOf(DISPLAY_A), service.subscribedDisplayIds)
        service.tick() // end the wait; the loop goes idle

        // The per-frame display check picks up the change on the next frame.
        service.displayIdToReturn = DISPLAY_B
        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 2 }
        assertEquals(listOf(DISPLAY_A, DISPLAY_B), service.subscribedDisplayIds)
        assertEquals(1, service.closeCount)
        service.tick()

        // Unchanged display: no subscription churn.
        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 3 }
        assertEquals(2, service.subscribeCount)
        assertEquals(1, service.closeCount)
    }

    @Test
    fun closesSubscriptionWhenTheDisplayBecomesUnresolvable() {
        val service = FakeFramePacingService().apply { periodNanos = HUGE_PERIOD_NANOS }
        onEdt {
            pacer = SwingRepaintPacer(panel, service)
            attachAndPack()
        }
        val baseline = onEdtGet { panel.repaintCount.get() }

        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 1 }
        assertEquals(1, service.subscribeCount)
        service.tick() // end the wait; the loop goes idle

        // The display id can no longer be resolved (per the service contract, -1 means unknown);
        // the next frame must drop the subscription and paint unpaced.
        service.displayIdToReturn = UNKNOWN_DISPLAY
        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 2 }
        assertEquals(1, service.closeCount)
    }

    @Test
    fun closesSubscriptionOnDisposeAndPassesThroughAfterwards() {
        val service = FakeFramePacingService().apply { periodNanos = HUGE_PERIOD_NANOS }
        onEdt {
            pacer = SwingRepaintPacer(panel, service)
            attachAndPack()
        }
        val baseline = onEdtGet { panel.repaintCount.get() }

        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 1 }
        assertEquals(1, service.subscribeCount)

        // Disposing mid-wait cancels the frame loop and closes the subscription.
        onEdt { pacer?.dispose() }
        waitUntil { onEdtGet { service.closeCount } == 1 }

        onEdt {
            val before = panel.repaintCount.get()
            pacer?.requestRepaint()
            assertEquals(before + 1, panel.repaintCount.get())
        }
        // A disposed pacer must not re-subscribe.
        settle()
        assertEquals(1, service.subscribeAttempts)
    }

    @Test
    fun releasesTheSubscriptionOnceTheComponentGoesIdle() {
        val service = FakeFramePacingService().apply { periodNanos = HUGE_PERIOD_NANOS }
        onEdt {
            pacer = SwingRepaintPacer(panel, service)
            attachAndPack()
        }
        val baseline = onEdtGet { panel.repaintCount.get() }

        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 1 }
        assertEquals(1, service.subscribeCount)

        // The tick ends the wait and nothing else is requested: the component is now idle, and the
        // subscription must be given back rather than held for the component's lifetime.
        service.tick()
        waitUntil(timeoutMillis = 10_000) { onEdtGet { service.closeCount } == 1 }

        // The next request paints straight away and takes a fresh subscription.
        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 2 }
        waitUntil { onEdtGet { service.subscribeCount } == 2 }
    }

    @Test
    fun keepsTheSubscriptionWhileTheFrameLoopIsWaiting() {
        val service = FakeFramePacingService().apply { periodNanos = HUGE_PERIOD_NANOS }
        onEdt {
            pacer = SwingRepaintPacer(panel, service)
            attachAndPack()
        }
        val baseline = onEdtGet { panel.repaintCount.get() }

        onEdt { pacer?.requestRepaint() }
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 1 }

        // The loop is suspended on a tick that never comes. Closing the subscription here would
        // strand it until its own timeout, which the huge period puts far beyond this test.
        Thread.sleep(2_000)
        assertEquals(0, onEdtGet { service.closeCount })

        // And the wait is still live: a tick still releases a repaint.
        onEdt { pacer?.requestRepaint() }
        service.tick()
        waitUntil { onEdtGet { panel.repaintCount.get() } == baseline + 2 }
    }

    @Test
    fun framePacingPropertyDefaultsToFalse() {
        assertFalse(SkikoProperties.swingFramePacingEnabled)
    }

    private fun onEdt(block: () -> Unit) = SwingUtilities.invokeAndWait(block)

    private fun <T> onEdtGet(block: () -> T): T {
        var result: T? = null
        SwingUtilities.invokeAndWait { result = block() }
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    /**
     * Lets in-flight frame-loop dispatches settle, for asserting that something did NOT happen.
     * The loop hops the EDT several times per frame (channel receive, resume, yield), so a single
     * queue drain is not enough.
     */
    private fun settle() {
        repeat(5) {
            Thread.sleep(20)
            SwingUtilities.invokeAndWait {}
        }
    }

    private fun waitUntil(timeoutMillis: Long = 5_000, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            Thread.sleep(10)
        }
        assertTrue(condition(), "Condition not met within $timeoutMillis ms")
    }

    /** Counts the no-argument [java.awt.Component.repaint] calls the pacer issues. */
    private class RepaintCountingPanel : JPanel() {
        val repaintCount = AtomicInteger()

        override fun repaint() {
            // JPanel's constructor calls repaint() before this class's fields are initialized.
            @Suppress("UNNECESSARY_SAFE_CALL", "SENSELESS_COMPARISON")
            repaintCount?.incrementAndGet()
            super.repaint()
        }
    }

    private class FakeFramePacingService : FramePacingService {
        var displayIdToReturn = DISPLAY_A
        var periodNanos = 1_000_000_000L
        var refuseSubscriptions = false
        var subscribeThrows = false

        /** Delivers one tick from the subscribe call itself, modelling a clock that ticks and then stalls. */
        var tickOnSubscribe = false

        @Volatile
        var subscribeAttempts = 0

        @Volatile
        var subscribeCount = 0

        @Volatile
        var closeCount = 0

        val subscribedDisplayIds = mutableListOf<Long>()

        private var listener: ((Long, Long) -> Unit)? = null

        override fun displayId(graphicsConfiguration: GraphicsConfiguration): Long =
            displayIdToReturn

        override fun refreshPeriodNanos(displayId: Long): Long = periodNanos

        override fun subscribe(
            displayId: Long,
            onTick: (displayId: Long, timeNanos: Long) -> Unit
        ): AutoCloseable? {
            subscribeAttempts++
            if (subscribeThrows) throw UnsatisfiedLinkError("no native clock")
            if (refuseSubscriptions) return null
            subscribedDisplayIds += displayId
            listener = onTick
            subscribeCount++
            if (tickOnSubscribe) {
                // On the tick source's own thread, like the real one, and joined so the test never
                // races the delivery.
                val delivery = Thread { onTick(displayId, System.nanoTime()) }
                delivery.start()
                delivery.join()
            }
            return AutoCloseable {
                if (listener === onTick) {
                    listener = null
                }
                closeCount++
            }
        }

        /** Delivers a tick the way the real service does: from a non-EDT thread. */
        fun tick() {
            check(!SwingUtilities.isEventDispatchThread())
            listener?.invoke(displayIdToReturn, System.nanoTime())
        }
    }

    private companion object {
        const val DISPLAY_A = 1L
        const val DISPLAY_B = 2L
        const val UNKNOWN_DISPLAY = -1L

        /** Keeps the tick timeout far beyond test duration where the wait must stay alive. */
        const val HUGE_PERIOD_NANOS = 100_000_000_000L
    }
}
