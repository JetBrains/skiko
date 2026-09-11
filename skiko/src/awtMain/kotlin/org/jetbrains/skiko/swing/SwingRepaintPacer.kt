package org.jetbrains.skiko.swing

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.MainUIDispatcher
import java.awt.Component
import java.awt.GraphicsConfiguration
import javax.swing.Timer
import kotlin.math.ceil

/**
 * Paces the repaints a component requests through [requestRepaint] to its display's refresh rate.
 *
 * A requested frame is repainted at once — a frame that is already wanted never waits for vsync — and the next one
 * is held until the display's next refresh tick. A continuous stream of requests is therefore repainted at most
 * once per tick, while a request that arrives when nothing is pending paints with no added latency.
 *
 * - If no pacing clock is available, or the component's display cannot be paced, requests fall through to plain
 *   [Component.repaint]s.
 * - The display is re-resolved from the component's [GraphicsConfiguration] on every frame, and the tick
 *   subscription follows it.
 * - A display that refuses a subscription, or accepts one and then never ticks, is not retried until the component
 *   moves to another display.
 * - The subscription is released once the component stops requesting frames, so an idle component does not keep a
 *   display clock running.
 * - A wait for a tick is bounded, so a clock that stops ticking cannot stall the frame loop.
 *
 * Only repaints requested through this class are paced. Paints Swing starts itself — on resize, expose,
 * `paintImmediately` — are not.
 *
 * [requestRepaint] and [dispose] must be called on the event dispatch thread.
 */
internal class SwingRepaintPacer(
    private val component: Component,
    private val service: FramePacingService? = SkikoFramePacingService.instance
) {
    private var disposed = false

    private var subscription: AutoCloseable? = null
    private var subscribedDisplayId = UNKNOWN_DISPLAY_ID

    /** The display that most recently refused a subscription or never ticked; not retried until the component moves. */
    private var failedDisplayId = UNKNOWN_DISPLAY_ID

    private var tickTimeoutMillis = DEFAULT_TICK_TIMEOUT_MILLIS

    /** Whether the current subscription has delivered a tick; tells a clock that stalled from one that never started. */
    private var receivedTick = false

    /**
     * Ticks reach the frame loop through a rendezvous: [Channel.trySend] succeeds only while the loop is suspended
     * in [Channel.receive], so a tick from before a wait started can never satisfy it. Each tick carries the
     * generation of the subscription that produced it, because a closed subscription can still deliver one tick
     * that was already in flight; a wait accepts only ticks of the current generation. Never closed, so a tick
     * arriving after [closeSubscription] is dropped rather than thrown.
     */
    private val tickChannel = Channel<Long>(Channel.RENDEZVOUS)
    private var subscriptionGeneration = 0L

    /**
     * Releases the subscription once the component has stopped requesting frames. The delay keeps a component that
     * invalidates in bursts — a blinking caret — on one subscription; only a genuinely idle one gives the clock up.
     * Stopped before every wait, so it can never fire while the loop is suspended on a tick.
     */
    private val idleTimer = Timer(IDLE_RELEASE_DELAY_MILLIS) { closeSubscription() }
        .apply { isRepeats = false }

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (!disposed) {
            component.repaint()
            awaitTickIfPaced()
        }
    }

    fun requestRepaint() {
        if (disposed || service == null) {
            component.repaint()
            return
        }

        frameDispatcher.scheduleFrame()
    }

    fun dispose() {
        if (disposed) return

        disposed = true
        idleTimer.stop()
        frameDispatcher.cancel()
        closeSubscription()
    }

    /**
     * Suspends until the display's next tick, so that the frame just painted is the only one in this refresh
     * interval. Does nothing when the display cannot be paced.
     *
     * When the wait times out the subscription is dropped; the frame was already painted, so nothing is owed. A
     * clock that ticked before and then stopped is treated as a transient stall, and the next frame subscribes
     * again. A clock that never ticked never started — a native source that could not be opened looks exactly like
     * this — and re-subscribing on every timeout would rebuild it forever while holding the scene to one repaint per
     * timeout. That display is instead remembered as unpaceable until the component moves.
     */
    private suspend fun awaitTickIfPaced() {
        val service = service ?: return

        idleTimer.stop()
        if (!ensureSubscription(service)) return

        // A fresh subscription's first tick also waits for the clock thread to start and the native source to
        // open, which the per-period timeout does not cover.
        val timeoutMillis = if (receivedTick) tickTimeoutMillis else maxOf(tickTimeoutMillis, FIRST_TICK_TIMEOUT_MILLIS)
        try {
            withTimeout(timeoutMillis) {
                while (tickChannel.receive() != subscriptionGeneration) {
                    // A tick from a subscription that has since been closed; keep waiting for the current one.
                }
            }
            receivedTick = true
            if (!disposed) idleTimer.restart()
        } catch (_: TimeoutCancellationException) {
            failedDisplayId = if (receivedTick) UNKNOWN_DISPLAY_ID else subscribedDisplayId
            closeSubscription()
        }
    }

    /** Returns false when the display cannot be resolved or paced, i.e. when there is no subscription to wait on. */
    private fun ensureSubscription(service: FramePacingService): Boolean {
        val displayId = resolveDisplayId(service)
        if (displayId == UNKNOWN_DISPLAY_ID || displayId == failedDisplayId) {
            closeSubscription()
            return false
        }

        if (subscription != null && displayId == subscribedDisplayId) return true

        closeSubscription()
        tickTimeoutMillis = tickTimeoutMillisFor(service.refreshPeriodNanos(displayId))
        receivedTick = false

        val generation = ++subscriptionGeneration
        val newSubscription = service.subscribe(displayId) { _, _ -> tickChannel.trySend(generation) }
        if (newSubscription == null) {
            failedDisplayId = displayId
            return false
        }

        subscription = newSubscription
        subscribedDisplayId = displayId
        failedDisplayId = UNKNOWN_DISPLAY_ID
        return true
    }

    private fun resolveDisplayId(service: FramePacingService): Long {
        val graphicsConfiguration = component.graphicsConfiguration ?: return UNKNOWN_DISPLAY_ID
        return service.displayId(graphicsConfiguration)
    }

    private fun closeSubscription() {
        subscription?.close()
        subscription = null
        subscribedDisplayId = UNKNOWN_DISPLAY_ID
    }

    private fun tickTimeoutMillisFor(periodNanos: Long): Long =
        if (periodNanos > 0) {
            ceil(TICK_TIMEOUT_PERIODS * periodNanos / 1_000_000.0)
                .toLong()
                .coerceAtLeast(MIN_TICK_TIMEOUT_MILLIS)
        } else {
            DEFAULT_TICK_TIMEOUT_MILLIS
        }

    companion object {
        private const val UNKNOWN_DISPLAY_ID = -1L
        private const val TICK_TIMEOUT_PERIODS = 3

        /** Tick timeout when the refresh period is unknown: three periods at 60 Hz. */
        private const val DEFAULT_TICK_TIMEOUT_MILLIS = 50L

        /**
         * Floor for the tick timeout. A variable-refresh display reports its maximum rate, but its actual tick gaps
         * can be several times longer (measured: up to 23.8 ms against a 5.56 ms nominal period on a 180 Hz panel),
         * so a timeout derived from the period alone would fire on a healthy clock.
         */
        private const val MIN_TICK_TIMEOUT_MILLIS = 50L

        /**
         * Budget for a fresh subscription's first tick. It has to cover the clock thread starting and the native
         * source opening — a DXGI enumeration on Windows — or a healthy clock that takes longer than the per-period
         * timeout to start would be remembered as unpaceable.
         */
        private const val FIRST_TICK_TIMEOUT_MILLIS = 500L

        /** How long a component may stop requesting frames before its subscription is released. */
        private const val IDLE_RELEASE_DELAY_MILLIS = 1_000
    }
}

/**
 * The tick source [SwingRepaintPacer] needs. An interface so tests can drive the pacer with a controllable clock;
 * production code uses [SkikoFramePacingService].
 */
internal interface FramePacingService {
    /**
     * Returns the stable id of the display showing [graphicsConfiguration], or -1 if unknown.
     */
    fun displayId(graphicsConfiguration: GraphicsConfiguration): Long

    /**
     * Returns the nominal refresh period of the display in nanoseconds, or 0 if unknown.
     */
    fun refreshPeriodNanos(displayId: Long): Long

    /**
     * Subscribes [onTick] to refresh ticks of [displayId]. [onTick] is invoked on an arbitrary non-EDT thread. Returns
     * a handle that closes the subscription, or null if the display cannot be paced.
     */
    fun subscribe(
        displayId: Long,
        onTick: (displayId: Long, timeNanos: Long) -> Unit
    ): AutoCloseable?
}
