package org.jetbrains.skiko.swing

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.MainUIDispatcher
import org.jetbrains.skiko.swing.SwingRepaintPacer.Companion.TICK_TIMEOUT_PERIODS
import java.awt.Component
import java.awt.GraphicsConfiguration
import javax.swing.Timer
import kotlin.math.ceil

/**
 * Paces invalidation-driven repaints of a Swing component to the display refresh, using Skiko's own display clocks
 * ([SkikoFramePacingService]), so it works on any JVM.
 *
 * Without pacing, a continuously invalidating scene (e.g., a draw-phase animation) calls [Component.repaint] as fast as
 * the EDT can complete paint cycles, rendering far above the display refresh rate and burning GPU on frames that are
 * never shown.
 *
 * The pacer runs a [FrameDispatcher] frame loop on the EDT, in the same shape as the redrawers' frame schedulers: a
 * requested frame is repainted immediately — a frame that is already wanted should never wait for vsync — and the
 * loop then suspends in [awaitTickIfPaced] until the next FramePacing tick before it repaints again. So pacing only
 * throttles a continuous stream of requests (to at most one repaint per tick), while a request arriving when the loop
 * is idle paints with no added latency.
 *
 * Behavior:
 * - If no pacing clock is available, or the component's display cannot be paced, requests degrade to unpaced
 *   [Component.repaint]s.
 * - The display id is re-resolved from the component's current [java.awt.GraphicsConfiguration] once per frame, and the
 *   tick subscription is re-created when it changes, comparing the display id per frame rather than listening to
 *   component hierarchy or property events.
 * - A display that refuses a subscription, or that accepts one and then never ticks, is remembered and not retried
 *   until the component moves to a different display, so an unpaceable display does not cost a subscription attempt
 *   per frame.
 * - The subscription is dropped once the component stops requesting frames, so an idle component does not hold a
 *   running display clock; the next request re-creates it, without added latency.
 * - The tick wait is bounded by [withTimeout], so a dead pacing clock can never stall the frame loop: see
 *   [awaitTickIfPaced].
 *
 * Note: this only gates the [Component.repaint] path. Paints initiated by Swing itself — resize, expose,
 * `paintImmediately` — are not affected.
 *
 * [requestRepaint] and [dispose] must be called on the event dispatch thread.
 */
internal class SwingRepaintPacer(
    private val component: Component,
    private val service: FramePacingService? = FramePacingServices.default
) {
    private var disposed = false

    private var subscription: AutoCloseable? = null
    private var subscribedDisplayId = UNKNOWN_DISPLAY_ID

    /**
     * The display that most recently refused a subscription, or accepted one and then never ticked; frames are not
     * paced while on it. Kept so that an unpaceable display does not cost a subscription attempt on every frame;
     * cleared when the component moves to another display, or when a tick timeout suggests the stall was transient.
     */
    private var failedDisplayId = UNKNOWN_DISPLAY_ID

    /**
     * ~[TICK_TIMEOUT_PERIODS] refresh periods of the subscribed display, never below
     * [MIN_TICK_TIMEOUT_MILLIS].
     */
    private var tickTimeoutMillis = DEFAULT_TICK_TIMEOUT_MILLIS

    /**
     * Whether the current subscription has delivered at least one tick. Written from the tick thread, read on the EDT
     * when a wait times out, to tell a clock that stalled from one that never started.
     */
    @Volatile
    private var receivedTick = false

    /**
     * The tick handoff to the frame loop: a tick is delivered only when the loop is suspended waiting for one, and
     * dropped otherwise — a tick from before a wait started must not satisfy it. RENDEZVOUS gives exactly that:
     * [Channel.trySend] succeeds only with a suspended receiver, so no tick is ever buffered. This is deliberately
     * the opposite of [FrameDispatcher]'s CONFLATED frame channel, where a request must never be dropped.
     *
     * Never closed: an in-flight [onTick] racing past [closeSubscription] must be able to send harmlessly into a
     * channel nobody reads anymore.
     */
    private val tickChannel = Channel<Unit>(Channel.RENDEZVOUS)

    /**
     * Drops the subscription once the component has stopped asking for frames.
     *
     * A subscription costs a running clock — a native thread in a vblank wait, and a callback into the JVM on every
     * refresh — so keeping one alive for the whole lifetime of a component that painted once works against the point
     * of pacing. The delay is what stops that from becoming churn: a component that invalidates in bursts (a blinking
     * caret) keeps its subscription between bursts, and only a genuinely idle one gives the clock up. Re-subscribing
     * costs nothing in latency, because a request arriving while the loop is idle paints before it waits.
     *
     * Restarted whenever a wait ends normally, and stopped before each wait, so it can never fire while the frame
     * loop is suspended on a tick.
     */
    private val idleTimer = Timer(IDLE_RELEASE_DELAY_MILLIS) { closeSubscription() }
        .apply { isRepeats = false }

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (!disposed) {
            component.repaint()
            awaitTickIfPaced()
        }
    }

    /**
     * Requests a repaint of the component. If the frame loop is idle, the repaint is issued immediately; while pacing
     * is active, a continuous stream of requests coalesces to at most one repaint per FramePacing tick.
     */
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
     * Suspends until the next FramePacing tick, so that the frame just painted is the only one in this refresh
     * interval. No-op when the display cannot be paced.
     *
     * The wait is bounded by [withTimeout] so that a dead pacing clock cannot stall the frame loop: a healthy clock
     * always beats the deadline, so the timeout only fires when the clock is not delivering. The loop never owes a
     * repaint at that point — the frame was already painted before the wait — so on timeout it stops waiting and
     * drops the subscription.
     *
     * What happens next depends on whether that subscription ever ticked. A clock that ticked and then stopped hit a
     * transient stall (display unplugged, mode change), so the next frame re-creates the subscription, retrying even
     * a previously refused display. A clock that never ticked at all never started: the backend accepted the display
     * and then stayed silent, which is what a native clock that failed to open its display source does. Re-creating
     * that one on every timeout would rebuild a native clock forever and hold the scene to one repaint per timeout,
     * which is slower than not pacing, so its display is remembered as unpaceable and retried only when the component
     * moves to another display.
     */
    private suspend fun awaitTickIfPaced() {
        val service = service ?: return

        // The subscription must stay alive for as long as this wait: the idle timer would otherwise close it and
        // leave the loop suspended until its own timeout.
        idleTimer.stop()

        if (!ensureSubscription(service)) return

        try {
            withTimeout(tickTimeoutMillis) { tickChannel.receive() }
            // The tick arrived. If no further frame is requested, this is where the component went idle, so start
            // counting down to giving the clock back.
            if (!disposed) idleTimer.restart()
        } catch (_: TimeoutCancellationException) {
            failedDisplayId = if (receivedTick) UNKNOWN_DISPLAY_ID else subscribedDisplayId
            closeSubscription()
        }
    }

    /**
     * The per-frame display check: re-resolves the component's display and re-creates the tick subscription if the
     * display changed. If the display cannot be resolved or paced, any live subscription is closed — its ticks belong
     * to a display the component is no longer on — and frames are not paced until the display can be resolved again.
     *
     * Returns false exactly when the display cannot be resolved or paced, i.e. when there is no subscription to wait on.
     */
    private fun ensureSubscription(service: FramePacingService): Boolean {
        val displayId = resolveDisplayId(service)
        if (displayId == UNKNOWN_DISPLAY_ID || displayId == failedDisplayId) {
            closeSubscription()
            return false
        }

        if (subscription != null && displayId == subscribedDisplayId) return true

        closeSubscription()
        tickTimeoutMillis = tickTimeoutMillisFor(service.refreshPeriodNanos(displayId))

        // Set before subscribing: the backend may tick as soon as the subscription exists.
        receivedTick = false
        // A backend that throws instead of returning null — a missing native entry point raises UnsatisfiedLinkError
        // here — has to degrade to unpaced repaints like any other refusal. An exception escaping the frame loop would
        // cancel it, and the layer would then stop repainting altogether.
        val newSubscription = try {
            service.subscribe(displayId) { _, _ -> onTick() }
        } catch (_: Throwable) {
            null
        }
        if (newSubscription == null) {
            failedDisplayId = displayId
            return false
        }

        subscription = newSubscription
        subscribedDisplayId = displayId
        failedDisplayId = UNKNOWN_DISPLAY_ID
        return true
    }

    /**
     * Called by the FramePacing service on a non-EDT thread; must return immediately.
     */
    private fun onTick() {
        receivedTick = true
        // Delivered only if the frame loop is suspended waiting for a tick; a tick with no waiter is dropped,
        // by design (see [tickChannel]).
        tickChannel.trySend(Unit)
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

        /**
         * Tick timeout when the refresh period is unknown: 3 periods at 60 Hz.
         */
        private const val DEFAULT_TICK_TIMEOUT_MILLIS = 50L

        /**
         * Absolute floor for the tick timeout. On a VRR display the refresh period reported by the
         * service is the nominal maximum-rate one, but adaptive refresh legitimately produces
         * single tick gaps several times that long (measured: up to 23.8 ms against a 5.56 ms
         * nominal period on a 180 Hz adaptive panel), so a purely period-derived timeout would
         * misfire on healthy clocks. 50 ms keeps the timeout meaningful at any refresh rate while
         * staying comfortably above every observed adaptive gap.
         */
        private const val MIN_TICK_TIMEOUT_MILLIS = 50L

        /**
         * How long a component may stop requesting frames before the pacer gives its clock back. Long enough that a
         * component invalidating in bursts keeps one subscription across the whole burst, short enough that an idle
         * window is not holding a running display clock.
         */
        private const val IDLE_RELEASE_DELAY_MILLIS = 1_000
    }
}

/**
 * The tick source [SwingRepaintPacer] needs. Kept as an interface so tests can drive the pacer with a controllable
 * clock; production code uses [SkikoFramePacingService].
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

/**
 * Resolves the [FramePacingService] the pacer uses: Skiko's own display clocks, so pacing works on any runtime.
 */
internal object FramePacingServices {
    val default: FramePacingService? by lazy { SkikoFramePacingService.instance }
}
