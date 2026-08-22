package org.jetbrains.skiko.swing

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.MainUIDispatcher
import org.jetbrains.skiko.swing.SwingRepaintPacer.Companion.TICK_TIMEOUT_PERIODS
import java.awt.Component
import java.awt.GraphicsConfiguration
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.math.ceil

/**
 * Paces invalidation-driven repaints of a Swing component to the display refresh, using the JBR `FramePacing` service
 * (available in JetBrains Runtime builds that provide `com.jetbrains.FramePacing`).
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
 * - If the JBR API or the FramePacing service is unavailable, or the component's display cannot be paced, requests
 *   degrade to unpaced [Component.repaint]s.
 * - The display id is re-resolved from the component's current [java.awt.GraphicsConfiguration] once per frame, and the
 *   tick subscription is re-created when it changes, following the FramePacing javadoc guidance to compare the display
 *   id per frame rather than listening to component hierarchy or property events.
 * - A display that refuses a subscription is remembered and not retried until the component moves to a different
 *   display, so an unpaceable display does not cost a subscription attempt per frame.
 * - The tick wait is bounded by [withTimeout] (~[TICK_TIMEOUT_PERIODS] refresh periods), so a dead pacing clock can
 *   never stall the frame loop: see [awaitTickIfPaced].
 *
 * Note: this only gates the [Component.repaint] path. Paints initiated by Swing itself — resize, expose,
 * `paintImmediately` — are not affected.
 *
 * [requestRepaint] and [dispose] must be called on the event dispatch thread.
 */
internal class SwingRepaintPacer(
    private val component: Component,
    private val service: FramePacingService? = JbrFramePacingApi.instance
) {
    private var disposed = false

    private var subscription: AutoCloseable? = null
    private var subscribedDisplayId = UNKNOWN_DISPLAY_ID

    /**
     * The display that most recently refused a subscription; frames are not paced while on it. Kept so that an
     * unpaceable display does not cost a subscription attempt on every frame; cleared when the component moves to
     * another display, or when a tick timeout suggests the stall was transient.
     */
    private var failedDisplayId = UNKNOWN_DISPLAY_ID

    /**
     * ~[TICK_TIMEOUT_PERIODS] refresh periods of the subscribed display.
     */
    private var tickTimeoutMillis = DEFAULT_TICK_TIMEOUT_MILLIS

    /**
     * Set on the EDT while the frame loop waits for a tick; resumed from the tick thread.
     */
    private val tickContinuation = AtomicReference<CancellableContinuation<Unit>?>(null)

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
        frameDispatcher.cancel()
        closeSubscription()
    }

    /**
     * Suspends until the next FramePacing tick, so that the frame just painted is the only one in this refresh
     * interval. No-op when the display cannot be paced.
     *
     * The wait is bounded by [withTimeout] so that a dead pacing clock cannot stall the frame loop: a healthy clock
     * always beats a ~3-period deadline, so the timeout only fires when the clock died mid-interval (display
     * unplugged, service stall). The loop never owes a repaint at that point — the frame was already painted before
     * the wait — so on timeout it simply stops waiting and drops the suspected-stale subscription; the next frame
     * re-creates it, retrying even a previously refused display.
     */
    private suspend fun awaitTickIfPaced() {
        val service = service ?: return

        ensureSubscription(service)
        if (subscription == null) return

        try {
            withTimeout(tickTimeoutMillis) {
                suspendCancellableCoroutine { continuation ->
                    tickContinuation.set(continuation)
                    continuation.invokeOnCancellation {
                        tickContinuation.compareAndSet(continuation, null)
                    }
                }
            }
        } catch (_: TimeoutCancellationException) {
            failedDisplayId = UNKNOWN_DISPLAY_ID
            closeSubscription()
        }
    }

    /**
     * The per-frame display check: re-resolves the component's display and re-creates the tick subscription if the
     * display changed. If the display cannot be resolved or paced, any live subscription is closed — its ticks belong
     * to a display the component is no longer on — and frames are not paced until the display can be resolved again.
     */
    private fun ensureSubscription(service: FramePacingService) {
        val displayId = resolveDisplayId(service)
        if (displayId == UNKNOWN_DISPLAY_ID || displayId == failedDisplayId) {
            closeSubscription()
            return
        }

        if (subscription != null && displayId == subscribedDisplayId) return

        subscribe(service, displayId)
    }

    private fun subscribe(service: FramePacingService, displayId: Long) {
        closeSubscription()

        tickTimeoutMillis = tickTimeoutMillisFor(service.refreshPeriodNanos(displayId))

        val subscription = service.subscribe(displayId) { _, _ -> onTick() }
        if (subscription == null) {
            failedDisplayId = displayId
            return
        }

        this.subscription = subscription
        subscribedDisplayId = displayId
        failedDisplayId = UNKNOWN_DISPLAY_ID
    }

    /**
     * Called by the FramePacing service on a non-EDT thread; must return immediately.
     */
    private fun onTick() {
        // A resume racing with cancellation (tick timeout, dispose) is benign: resuming a canceled continuation
        // is ignored.
        tickContinuation.getAndSet(null)?.resume(Unit)
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
        private const val MIN_TICK_TIMEOUT_MILLIS = 10L
    }
}

/**
 * The subset of the JBR `com.jetbrains.FramePacing` service that [SwingRepaintPacer] uses. Abstracted so that tests
 * can drive the pacer with a controllable tick source; production code uses [JbrFramePacingApi], which adapts the real
 * service reflectively until a jbr-api release ships the FramePacing service accessor.
 *
 * TODO replace with a direct FramePacing service accessor once a jbr-api 1.11 release ships it.
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
 * Reflection-based access to the JBR `com.jetbrains.FramePacing` service, so that Skiko does not need a compile-time
 * dependency on a jbr-api version that provides it. Resolved once; [instance] is null when the API classes are absent
 * from the classpath or the runtime does not provide the service.
 *
 * TODO replace with a direct FramePacing service accessor once a jbr-api 1.11 release ships it.
 */
private class JbrFramePacingApi private constructor(
    private val service: Any,
    private val displayIdMethod: Method,
    private val refreshPeriodNanosMethod: Method,
    private val subscribeMethod: Method,
    private val listenerClass: Class<*>,
    private val subscriptionCloseMethod: Method
) : FramePacingService {

    override fun displayId(graphicsConfiguration: GraphicsConfiguration): Long =
        displayIdMethod.invoke(service, graphicsConfiguration) as Long

    override fun refreshPeriodNanos(displayId: Long): Long =
        refreshPeriodNanosMethod.invoke(service, displayId) as Long

    override fun subscribe(
        displayId: Long,
        onTick: (displayId: Long, timeNanos: Long) -> Unit
    ): AutoCloseable? {
        val listener = Proxy.newProxyInstance(
            listenerClass.classLoader,
            arrayOf(listenerClass)
        ) { proxy, method, args ->
            when (method.name) {
                "onTick" -> {
                    onTick(args[0] as Long, args[1] as Long)
                    null
                }

                "equals" -> proxy === args[0]
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "SwingRepaintPacer.Listener"
                else -> null
            }
        }
        val subscription = subscribeMethod.invoke(service, displayId, listener) ?: return null
        return AutoCloseable {
            subscriptionCloseMethod.invoke(subscription)
        }
    }

    companion object {
        val instance: FramePacingService? by lazy {
            try {
                val jbrClass = Class.forName("com.jetbrains.JBR")
                val service = jbrClass.getMethod("getFramePacing").invoke(null)
                    ?: return@lazy null

                val serviceClass = Class.forName("com.jetbrains.FramePacing")
                val listenerClass = Class.forName("com.jetbrains.FramePacing\$Listener")
                val subscriptionClass = Class.forName("com.jetbrains.FramePacing\$Subscription")

                JbrFramePacingApi(
                    service = service,
                    displayIdMethod = serviceClass.getMethod(
                        "displayId",
                        GraphicsConfiguration::class.java
                    ),
                    refreshPeriodNanosMethod = serviceClass.getMethod(
                        "refreshPeriodNanos",
                        Long::class.javaPrimitiveType
                    ),
                    subscribeMethod = serviceClass.getMethod(
                        "subscribe",
                        Long::class.javaPrimitiveType,
                        listenerClass
                    ),
                    listenerClass = listenerClass,
                    subscriptionCloseMethod = subscriptionClass.getMethod("close")
                )
            } catch (_: Throwable) {
                null
            }
        }
    }
}
