package org.jetbrains.skiko.swing

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.SkikoProperties
import org.jetbrains.skiko.hostOs
import java.awt.GraphicsConfiguration
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport

/**
 * Skiko-owned implementation of [FramePacingService]: per-display tick clocks living entirely
 * inside Skiko. This is the pacer's only tick source, so pacing works on any JVM.
 *
 * One native clock per platform, each bound to a single display:
 * - macOS: a Skiko-owned per-display `CADisplayLink` (`NSScreen.displayLink`, macOS 14+) — a true, VRR-aware display clock (CVDisplayLink is deprecated since macOS 15 and deliberately not used).
 * - Windows: `IDXGIOutput::WaitForVBlank` on the output belonging to the clock's display — a true
 *   per-display hardware vblank, correct on mixed-refresh setups. When DXGI has no desktop-attached
 *   outputs (remote sessions), DWM composition timing is the fallback cadence (one desktop-wide
 *   rate; never in-process `DwmFlush`, which can stall for hundreds of milliseconds when the client
 *   gates presents on the same tick). With neither, the same native thread paces at the nominal
 *   rate on Windows' high-resolution waitable timer — deliberately not a JVM-side wait, which
 *   stock JVMs quantize to the ~16 ms system timer.
 * - Linux: a per-CRTC kernel DRM vblank clock — kernel vblank timestamps, free-running
 *   regardless of compositor activity, bound to the CRTC whose mode period matches the
 *   display's advertised refresh. Anything else: a phase-aligned timer at the nominal rate.
 *
 * Display ids come from public AWT API only ([GraphicsDevice.getIDstring]): on macOS the string
 * embeds the `CGDirectDisplayID`, on Windows the AWT screen index (which follows the system's
 * `EnumDisplayMonitors` order, the same order the native side indexes into). No `sun.awt` access.
 *
 * Deliberate simplifications, leaning on [SwingRepaintPacer]'s own safety nets:
 * - Listeners are held strongly: subscriptions are closed deterministically by the pacer, so the
 *   weak-reference registry and its GC lifecycle machinery are unnecessary.
 * - Clocks never self-stop: display hotplug is handled by the pacer's per-frame display
 *   re-resolution, and a clock whose display died is recovered by the pacer's tick timeout. A
 *   vanished display's native source degrades to nominal-rate ticking rather than starving.
 */
internal class SkikoFramePacingService private constructor(
    private val newClock: (displayId: Long, periodNanos: Long, reportedPeriodNanos: Long) -> DisplayClock
) : FramePacingService {

    private val clocks = HashMap<Long, DisplayClock>()

    override fun displayId(graphicsConfiguration: GraphicsConfiguration): Long =
        deviceDisplayId(graphicsConfiguration.device)

    override fun refreshPeriodNanos(displayId: Long): Long {
        val device = findDevice(displayId) ?: return 0
        val rate = device.displayMode?.refreshRate ?: 0
        // AWT reports 0 for an unknown rate, and a driver can report a nonsense one. Anything
        // outside the plausible band is treated as unknown rather than trusted: the period drives
        // the timer cadence, the pacer's tick timeout and the Linux CRTC match, so a bogus 1 Hz
        // would poison all three.
        if (rate < MIN_PLAUSIBLE_REFRESH_HZ || rate > MAX_PLAUSIBLE_REFRESH_HZ) return 0
        return 1_000_000_000L / rate
    }

    @Synchronized
    override fun subscribe(
        displayId: Long,
        onTick: (displayId: Long, timeNanos: Long) -> Unit
    ): AutoCloseable? {
        if (displayId == -1L || findDevice(displayId) == null) return null

        val clock = clocks.getOrPut(displayId) {
            // Two periods, deliberately. The tick period substitutes a 60 Hz default so the timer
            // backend always has a cadence to run at. The reported period is passed through as it
            // is, including 0 for "unknown", because a backend that matches hardware against it
            // (the Linux CRTC binding) must not match against an invented figure.
            val reported = refreshPeriodNanos(displayId)
            newClock(displayId, if (reported > 0) reported else FALLBACK_PERIOD_NANOS, reported)
        }
        clock.add(onTick)

        val closed = AtomicBoolean(false)
        return AutoCloseable {
            if (closed.compareAndSet(false, true)) {
                unsubscribe(clock, onTick)
            }
        }
    }

    @Synchronized
    private fun unsubscribe(clock: DisplayClock, listener: TickListener) {
        if (clock.remove(listener)) {
            clocks.remove(clock.displayId, clock)
        }
    }

    private fun findDevice(displayId: Long): GraphicsDevice? {
        if (displayId == -1L) return null
        if (GraphicsEnvironment.isHeadless()) return null

        return GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
            .firstOrNull { it.type == GraphicsDevice.TYPE_RASTER_SCREEN && deviceDisplayId(it) == displayId }
    }

    companion object {
        private const val FALLBACK_PERIOD_NANOS = 1_000_000_000L / 60

        /** The band of refresh rates [refreshPeriodNanos] is willing to believe. */
        private const val MIN_PLAUSIBLE_REFRESH_HZ = 20
        private const val MAX_PLAUSIBLE_REFRESH_HZ = 1_000

        /**
         * The service with the best backend this platform offers, or null when the environment
         * cannot be paced at all (headless).
         */
        val instance: FramePacingService? by lazy {
            if (GraphicsEnvironment.isHeadless()) return@lazy null

            val forceTimer = SkikoProperties.swingFramePacingForceTimer
            SkikoFramePacingService { displayId, periodNanos, reportedPeriodNanos ->
                createClock(displayId, periodNanos, reportedPeriodNanos, forceTimer)
            }
        }

        /**
         * The clock backend for [displayId]: the best native source this platform offers, or the
         * phase-aligned timer where there is none. [periodNanos] is the cadence a timer would run
         * at; [reportedPeriodNanos] is what AWT actually reported, 0 when it reported nothing.
         *
         * Separate from [instance] so tests can assert which backend a display gets, rather than
         * only that the probe accepted it.
         */
        internal fun createClock(
            displayId: Long,
            periodNanos: Long,
            reportedPeriodNanos: Long,
            forceTimer: Boolean
        ): DisplayClock = when {
            forceTimer -> TimerClock(displayId, periodNanos)

            hostOs == OS.MacOS && MacDisplayLinkClock.available(displayId) ->
                MacDisplayLinkClock(displayId, periodNanos)

            hostOs == OS.Windows && WinNativeClock.vblankAvailable() ->
                WinNativeClock(displayId, periodNanos, useVBlank = true)

            // Even without DWM the native clock paces on Windows' high-resolution waitable timer,
            // which a JVM-side wait cannot match on stock JVMs (~16 ms system-timer quantization).
            hostOs == OS.Windows ->
                WinNativeClock(displayId, periodNanos, useVBlank = false)

            hostOs == OS.Linux && LinuxDrmVBlankClock.available() ->
                LinuxDrmVBlankClock(displayId, periodNanos, reportedPeriodNanos)

            else -> TimerClock(displayId, periodNanos)
        }

        /**
         * The platform display id for [device], parsed out of the public
         * [GraphicsDevice.getIDstring]:
         * - macOS `CGraphicsDevice`: `"Display <CGDirectDisplayID>"` — the native display id.
         * - Windows `Win32GraphicsDevice`: `"\\Display<screen>"` — the AWT screen index.
         * - Anywhere else, any stable non-negative hash of the id string works: it only ever keys
         *   the clock registry and the timer backend.
         */
        internal fun deviceDisplayId(device: GraphicsDevice?): Long {
            if (device == null || device.type != GraphicsDevice.TYPE_RASTER_SCREEN) return -1
            val idString = device.iDstring ?: return -1

            val trailingDigits = idString.takeLastWhile { it.isDigit() }
            return when {
                hostOs == OS.MacOS || hostOs == OS.Windows ->
                    if (trailingDigits.isEmpty()) -1 else trailingDigits.toLongOrNull() ?: -1

                else -> idString.hashCode().toLong() and 0xFFFFFFFFL
            }
        }
    }
}

private typealias TickListener = (displayId: Long, timeNanos: Long) -> Unit

/**
 * Runs the clocks' native teardown off the thread that dropped the last listener.
 *
 * Subscriptions are closed on the event dispatch thread, and releasing a native clock joins its
 * clock thread. That join is bounded at one second, but a wedged clock thread — which is exactly
 * the case the pacer's tick timeout exists for — would spend that whole second on the EDT and
 * freeze the UI. Stopping is cheap and stays synchronous; only the join moves here.
 */
private val clockReaper: java.util.concurrent.Executor by lazy {
    java.util.concurrent.Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "Skiko-FramePacing-Reaper").apply { isDaemon = true }
    }
}

/**
 * One tick source per display: keeps the listener list, starts the source with the first listener,
 * stops it when the last one is removed. All add/remove calls happen under the service lock.
 */
internal abstract class DisplayClock(
    val displayId: Long,
    val periodNanos: Long
) {
    private val listeners = CopyOnWriteArrayList<TickListener>()

    @Volatile
    protected var stopped = false
        private set

    private var started = false

    fun add(listener: TickListener) {
        listeners.add(listener)
        if (!started) {
            started = true
            onStart()
        }
    }

    /**
     * @return true when the last listener was removed and the clock stopped
     */
    fun remove(listener: TickListener): Boolean {
        listeners.remove(listener)
        if (listeners.isNotEmpty()) return false

        stopped = true
        onStop()
        // Handing the task to the executor also publishes everything written above it, so the
        // reaper sees the same state the caller left behind.
        clockReaper.execute(::onRelease)
        return true
    }

    /** Starts the tick source. Called with the first listener added. */
    protected abstract fun onStart()

    /**
     * Signals the tick source to stop. Called on the caller's thread — the event dispatch thread,
     * in production — after [stopped] is set, so it must not block.
     */
    protected abstract fun onStop()

    /**
     * Frees the tick source. Called on the reaper thread after [onStop], because native teardown
     * joins the clock thread and must never do that on the EDT.
     */
    protected open fun onRelease() = Unit

    /**
     * Delivers one tick to all listeners; exceptions are isolated per listener. Tick sources call
     * this once per display refresh; delivery is skipped after the clock stopped.
     */
    protected fun deliver(timeNanos: Long) {
        if (stopped) return

        for (listener in listeners) {
            try {
                listener(displayId, timeNanos)
            } catch (_: Throwable) {
                // Listeners must not break the clock or each other.
            }
        }
    }
}

/**
 * The estimated tick source: a daemon thread parked until the next period boundary of a
 * phase-aligned deadline grid. Missed periods are skipped, never queued.
 */
internal class TimerClock(
    displayId: Long,
    periodNanos: Long
) : DisplayClock(displayId, periodNanos), Runnable {

    @Volatile
    private var thread: Thread? = null

    override fun onStart() {
        val thread = Thread(this, "Skiko-FramePacing-$displayId")
        thread.isDaemon = true
        this.thread = thread
        thread.start()
    }

    override fun onStop() {
        // The thread observes the stopped flag, but it is parked until its next deadline and would
        // otherwise linger for up to a full period. Unparking makes it wind down at once.
        thread?.let { LockSupport.unpark(it) }
        thread = null
    }

    override fun run() {
        // Phase-aligned wait loop: park until the next period boundary, then deliver. parkNanos
        // may wake early or spuriously, so every wake re-checks the deadline and re-parks for the
        // remainder — tick timing is gated by the monotonic-clock comparison, not by park
        // precision.
        var deadline = System.nanoTime() + periodNanos

        while (!stopped) {
            val now = System.nanoTime()
            if (now < deadline) {
                LockSupport.parkNanos(deadline - now)
                continue
            }

            // Woke past the deadline: advance it to the next future period boundary, skipping any
            // fully missed periods rather than delivering catch-up bursts.
            deadline += ((now - deadline) / periodNanos + 1) * periodNanos

            deliver(now)
        }
    }
}

/**
 * macOS display clock: a Skiko-owned per-display `CADisplayLink` (`NSScreen.displayLink`, macOS 14+) delivering
 * [onNativeTick] from the display link callback thread. See `FramePacing.mm`.
 */
internal class MacDisplayLinkClock(
    displayId: Long,
    periodNanos: Long
) : DisplayClock(displayId, periodNanos) {

    private var ptr = 0L

    override fun onStart() {
        ptr = nativeCreate(displayId.toInt(), this)
        if (ptr != 0L) {
            nativeStart(ptr)
        }
        // On failure the clock stays silent; the pacer's tick timeout drops the subscription.
    }

    override fun onStop() {
        if (ptr != 0L) nativeStop(ptr)
    }

    override fun onRelease() {
        if (ptr != 0L) {
            nativeRelease(ptr)
            ptr = 0L
        }
    }

    /** Called from the CADisplayLink runloop thread. */
    @Suppress("unused") // called from native
    fun onNativeTick(timeNanos: Long) = deliver(timeNanos)

    companion object {
        fun available(displayId: Long): Boolean =
            try {
                nativeProbe(displayId.toInt())
            } catch (_: UnsatisfiedLinkError) {
                false
            }

        @JvmStatic
        private external fun nativeProbe(displayId: Int): Boolean

        @JvmStatic
        private external fun nativeCreate(displayId: Int, clock: MacDisplayLinkClock): Long

        @JvmStatic
        private external fun nativeStart(ptr: Long)

        @JvmStatic
        private external fun nativeStop(ptr: Long)

        @JvmStatic
        private external fun nativeRelease(ptr: Long)
    }
}

/**
 * Windows display clock, in one of two native flavors (see `FramePacing.cc`):
 * - [useVBlank]: `IDXGIOutput::WaitForVBlank` on the output of the AWT screen [displayId] —
 *   a true per-display vblank.
 * - otherwise: DWM composition timing waited out with a high-resolution waitable timer — one
 *   desktop-wide cadence, for DXGI-less environments.
 */
internal class WinNativeClock(
    displayId: Long,
    periodNanos: Long,
    private val useVBlank: Boolean
) : DisplayClock(displayId, periodNanos) {

    private var ptr = 0L

    override fun onStart() {
        ptr = if (useVBlank) {
            nativeCreateVBlank(this, displayId.toInt(), periodNanos)
        } else {
            nativeCreate(this, periodNanos)
        }
        if (ptr != 0L) {
            nativeStart(ptr)
        }
        // On failure the clock stays silent; the pacer's tick timeout drops the subscription.
    }

    override fun onStop() {
        if (ptr != 0L) nativeStop(ptr)
    }

    override fun onRelease() {
        if (ptr != 0L) {
            nativeRelease(ptr)
            ptr = 0L
        }
    }

    /** Called from the native clock thread. */
    @Suppress("unused") // called from native
    fun onNativeTick(timeNanos: Long) = deliver(timeNanos)

    companion object {
        fun vblankAvailable(): Boolean =
            try {
                nativeProbeVBlank()
            } catch (_: UnsatisfiedLinkError) {
                false
            }

        @JvmStatic
        private external fun nativeProbeVBlank(): Boolean

        @JvmStatic
        private external fun nativeCreate(clock: WinNativeClock, fallbackPeriodNanos: Long): Long

        @JvmStatic
        private external fun nativeCreateVBlank(clock: WinNativeClock, screen: Int, fallbackPeriodNanos: Long): Long

        @JvmStatic
        private external fun nativeStart(ptr: Long)

        @JvmStatic
        private external fun nativeStop(ptr: Long)

        @JvmStatic
        private external fun nativeRelease(ptr: Long)
    }
}

/**
 * Linux display clock: a per-CRTC kernel DRM vblank wait delivering kernel vblank timestamps
 * (see `FramePacing.cc` in `cpp/linux`). The CRTC is chosen by matching mode periods against
 * the display's advertised refresh; needs a local session with DRM device access (logind seat
 * ACL), so remote/headless environments probe unavailable and fall back to the timer.
 *
 * [displayPeriodNanos] is the refresh period AWT reported for this display, or 0 when it reported
 * none. It is the CRTC match key, and it is deliberately not the substituted [periodNanos]: with
 * an unknown rate, matching against a stand-in 60 Hz would pick the 60 Hz CRTC on a mixed-refresh
 * desktop and pace a 144 Hz window at 60. With 0 the native side takes the first active CRTC
 * instead, which is right on a single-display desktop and no worse anywhere else.
 */
internal class LinuxDrmVBlankClock(
    displayId: Long,
    periodNanos: Long,
    private val displayPeriodNanos: Long
) : DisplayClock(displayId, periodNanos) {

    private var ptr = 0L

    override fun onStart() {
        ptr = nativeCreate(this, displayPeriodNanos, null)
        if (ptr != 0L) {
            nativeStart(ptr)
        }
        // On failure the clock stays silent; the pacer's tick timeout drops the subscription.
    }

    override fun onStop() {
        if (ptr != 0L) nativeStop(ptr)
    }

    override fun onRelease() {
        if (ptr != 0L) {
            nativeRelease(ptr)
            ptr = 0L
        }
    }

    /** Called from the DRM vblank wait thread. */
    @Suppress("unused") // called from native
    fun onNativeTick(timeNanos: Long) = deliver(timeNanos)

    companion object {
        fun available(): Boolean =
            try {
                nativeProbe()
            } catch (_: UnsatisfiedLinkError) {
                false
            }

        @JvmStatic
        private external fun nativeProbe(): Boolean

        @JvmStatic
        private external fun nativeCreate(
            clock: LinuxDrmVBlankClock,
            displayPeriodNanos: Long,
            connectorName: String?
        ): Long

        @JvmStatic
        private external fun nativeStart(ptr: Long)

        @JvmStatic
        private external fun nativeStop(ptr: Long)

        @JvmStatic
        private external fun nativeRelease(ptr: Long)
    }
}
