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
 * [FramePacingService] backed by one clock per display, each reading that display's own vblank:
 * - macOS: `CADisplayLink` (`NSScreen.displayLink`, macOS 14+), which also follows adaptive refresh
 *   rates.
 * - Windows: `IDXGIOutput::WaitForVBlank` on the output that drives the display.
 * - Linux: the kernel DRM vblank of the CRTC whose mode period matches the display's refresh rate.
 *
 * Where none of these is available — macOS 13 and older, a Linux session without DRM device
 * access — a timer at the nominal refresh rate stands in. Windows has no stand-in: a display
 * whose DXGI output cannot be opened is not paced. None turned up in testing: a Remote Desktop
 * session and a VM with no GPU at all both still expose an output, and `WaitForVBlank` blocks
 * there at the session's rate.
 *
 * A display whose refresh rate the toolkit does not report is not paced by the timer or on Linux,
 * where the rate is what the CRTC is matched on. The macOS and Windows sources are per-display
 * and do not need it.
 *
 * Display ids come from public AWT API only ([GraphicsDevice.getIDstring]): on macOS the string
 * embeds the `CGDirectDisplayID`, on Windows the AWT screen index, which follows the system's
 * `EnumDisplayMonitors` order — the same order the native side indexes into.
 */
internal class SkikoFramePacingService private constructor(
    private val newClock: (displayId: Long, periodNanos: Long) -> DisplayClock?
) : FramePacingService {

    private val clocks = HashMap<Long, DisplayClock>()

    override fun displayId(graphicsConfiguration: GraphicsConfiguration): Long =
        deviceDisplayId(graphicsConfiguration.device)

    override fun refreshPeriodNanos(displayId: Long): Long {
        val device = findDevice(displayId) ?: return 0
        val rate = device.displayMode?.refreshRate ?: 0
        // AWT reports 0 for an unknown rate, and a driver can report a nonsense one. Either is
        // treated as unknown: the period drives the timer cadence, the pacer's tick timeout and the
        // Linux CRTC match, so a bogus 1 Hz would poison all three.
        if (rate !in MIN_PLAUSIBLE_REFRESH_HZ..MAX_PLAUSIBLE_REFRESH_HZ) return 0
        return 1_000_000_000L / rate
    }

    @Synchronized
    override fun subscribe(
        displayId: Long,
        onTick: (displayId: Long, timeNanos: Long) -> Unit
    ): AutoCloseable? {
        if (displayId == -1L || findDevice(displayId) == null) return null

        val clock = clocks[displayId] ?: run {
            val created = newClock(displayId, refreshPeriodNanos(displayId)) ?: return null
            clocks[displayId] = created
            created
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
        /** The band of refresh rates [refreshPeriodNanos] is willing to believe. */
        private const val MIN_PLAUSIBLE_REFRESH_HZ = 20
        internal const val MAX_PLAUSIBLE_REFRESH_HZ = 2_000

        /**
         * The service with the best backend this platform offers, or null when the environment
         * cannot be paced at all (headless).
         */
        val instance: FramePacingService? by lazy {
            if (GraphicsEnvironment.isHeadless()) return@lazy null

            val forceTimer = SkikoProperties.swingFramePacingForceTimer
            SkikoFramePacingService { displayId, periodNanos -> createClock(displayId, periodNanos, forceTimer) }
        }

        /**
         * The clock for [displayId], or null when it cannot be paced. [periodNanos] is the display's
         * refresh period, 0 when the toolkit reported none; the timer and the Linux CRTC match need
         * it, the macOS and Windows sources do not.
         *
         * Separate from [instance] so tests can assert which backend a display gets.
         */
        internal fun createClock(displayId: Long, periodNanos: Long, forceTimer: Boolean): DisplayClock? = when {
            forceTimer -> timerOrNull(displayId, periodNanos)

            hostOs == OS.MacOS && MacDisplayLinkClock.available(displayId) ->
                MacDisplayLinkClock(displayId, periodNanos)

            hostOs == OS.Windows -> WinVBlankClock(displayId, periodNanos)

            hostOs == OS.Linux && periodNanos > 0 && LinuxDrmVBlankClock.available() ->
                LinuxDrmVBlankClock(displayId, periodNanos)

            else -> timerOrNull(displayId, periodNanos)
        }

        private fun timerOrNull(displayId: Long, periodNanos: Long): DisplayClock? =
            if (periodNanos > 0) TimerClock(displayId, periodNanos) else null

        /**
         * The platform display id for [device], parsed out of [GraphicsDevice.getIDstring]:
         * - macOS `CGraphicsDevice`: `"Display <CGDirectDisplayID>"`.
         * - Windows `Win32GraphicsDevice`: `"\\Display<screen>"`, the AWT screen index.
         * - Anywhere else, a stable hash of the id string: it only keys the clock registry.
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
 * One tick source per display: keeps the listener list, starts the source with the first listener
 * and stops it when the last one is removed. [add] and [remove] are called under the service lock.
 * [periodNanos] is the display's refresh period, 0 when the toolkit reported none.
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
        return true
    }

    protected abstract fun onStart()

    /** Called after [stopped] is set, on the caller's thread; must not block. */
    protected abstract fun onStop()

    /** Delivers one tick to every listener. A throwing listener does not stop the others. */
    protected fun deliver(timeNanos: Long) {
        for (listener in listeners) {
            // Re-checked per listener: the list is a snapshot, and the clock can stop mid-delivery.
            if (stopped) return
            try {
                listener(displayId, timeNanos)
            } catch (_: Throwable) {
            }
        }
    }
}

/**
 * A timer at the nominal refresh rate, on a phase-aligned deadline grid. Missed periods are skipped,
 * never queued.
 */
internal class TimerClock(
    displayId: Long,
    periodNanos: Long
) : DisplayClock(displayId, periodNanos), Runnable {

    init {
        require(periodNanos > 0) { "a timer clock needs a refresh period" }
    }

    @Volatile
    private var thread: Thread? = null

    override fun onStart() {
        val thread = Thread(this, "Skiko-FramePacing-$displayId")
        thread.isDaemon = true
        this.thread = thread
        thread.start()
    }

    override fun onStop() {
        // The thread is parked until its next deadline; unparking it makes it exit at once.
        thread?.let { LockSupport.unpark(it) }
        thread = null
    }

    override fun run() {
        var deadline = System.nanoTime() + periodNanos

        while (!stopped && !Thread.currentThread().isInterrupted) {
            val now = System.nanoTime()
            if (now < deadline) {
                // parkNanos may wake early or spuriously; the deadline comparison decides, not the park.
                LockSupport.parkNanos(deadline - now)
                // An interrupt ends the clock: parkNanos returns at once while the flag is set, and
                // the loop would otherwise spin until the deadline.
                if (Thread.interrupted()) return
                continue
            }

            deadline += ((now - deadline) / periodNanos + 1) * periodNanos
            deliver(now)
        }
    }
}

/**
 * A clock that blocks on a native vblank source from its own daemon thread. That thread opens the
 * source, waits on it in a loop and closes it when the clock stops, so no other thread ever touches
 * the native handle and nothing has to be joined.
 *
 * A source that cannot be opened leaves the clock silent, and one that fails while running ends the
 * thread. Either way the pacer's tick timeout drops the subscription. A wait the driver does not
 * return keeps the thread and its source until it does; nothing waits on that thread, and a
 * re-subscribe to a vanished display fails to open a source rather than starting another one.
 * An interrupt ends the clock.
 */
internal abstract class NativeVBlankClock(
    displayId: Long,
    periodNanos: Long
) : DisplayClock(displayId, periodNanos) {

    @Volatile
    private var thread: Thread? = null

    /** Opens the source. Returns 0 when the display has no usable one. */
    protected abstract fun open(): Long

    /**
     * Blocks until the next vblank and returns its timestamp on the [System.nanoTime] scale; 0 when
     * the wait ended without a tick, negative when the source is gone.
     */
    protected abstract fun waitTick(handle: Long): Long

    protected abstract fun close(handle: Long)

    override fun onStart() {
        val thread = Thread({ run() }, "Skiko-FramePacing-$displayId")
        thread.isDaemon = true
        this.thread = thread
        thread.start()
    }

    override fun onStop() {
        thread?.let { LockSupport.unpark(it) }
        thread = null
    }

    private fun run() {
        val handle = open()
        if (handle == 0L) return

        try {
            var last = 0L
            var hasLast = false
            while (!stopped && !Thread.currentThread().isInterrupted) {
                var tick = waitTick(handle)
                if (stopped || tick < 0) break
                if (tick == 0L) continue

                // A display in power save can complete every wait at once. No real display ticks at
                // twice its nominal rate, so anything faster is not a vblank: hold to the nominal period
                // until real ticks resume. Half the period, because a true refresh rate runs slightly
                // above its nominal figure. With no known period, only a rate faster than any display
                // counts as a spin, and it is held to a 60 Hz fallback.
                val elapsed = tick - last
                val spinLimit = if (periodNanos > 0) periodNanos / 2 else IMPOSSIBLE_INTERVAL_NANOS
                if (hasLast && elapsed < spinLimit) {
                    val hold = if (periodNanos > 0) periodNanos else FALLBACK_HOLD_NANOS
                    LockSupport.parkNanos(hold - elapsed.coerceAtLeast(0))
                    // parkNanos returns at once while the interrupt flag is set, which would turn this
                    // hold into a spin; an interrupt ends the clock instead.
                    if (stopped || Thread.interrupted()) break
                    tick = System.nanoTime()
                }

                last = tick
                hasLast = true
                deliver(tick)
            }
        } finally {
            close(handle)
        }
    }

    private companion object {
        /** No display ticks faster than the fastest plausible refresh rate; anything faster is a spin. */
        const val IMPOSSIBLE_INTERVAL_NANOS = 1_000_000_000L / SkikoFramePacingService.MAX_PLAUSIBLE_REFRESH_HZ

        /** What a spinning source with no known period is held to. */
        const val FALLBACK_HOLD_NANOS = 1_000_000_000L / 60
    }
}

/** `CADisplayLink` for the display; see `FramePacing.mm`. */
internal class MacDisplayLinkClock(
    displayId: Long,
    periodNanos: Long
) : NativeVBlankClock(displayId, periodNanos) {

    override fun open(): Long = nativeOpen(displayId.toInt())

    override fun waitTick(handle: Long): Long = nativeWaitTick(handle)

    override fun close(handle: Long) = nativeClose(handle)

    companion object {
        fun available(displayId: Long): Boolean = nativeProbe(displayId.toInt())

        @JvmStatic
        private external fun nativeProbe(displayId: Int): Boolean

        @JvmStatic
        private external fun nativeOpen(displayId: Int): Long

        @JvmStatic
        private external fun nativeWaitTick(handle: Long): Long

        @JvmStatic
        private external fun nativeClose(handle: Long)
    }
}

/**
 * `IDXGIOutput::WaitForVBlank` on the output that drives the AWT screen; see `FramePacing.cc`. A
 * display whose output cannot be found cannot be opened; neither a Remote Desktop session nor a
 * VM without a GPU is such a case, their outputs exist like any other (measured: 32 and 64 Hz).
 */
internal class WinVBlankClock(
    displayId: Long,
    periodNanos: Long
) : NativeVBlankClock(displayId, periodNanos) {

    override fun open(): Long = nativeOpen(displayId.toInt())

    override fun waitTick(handle: Long): Long = nativeWaitTick(handle)

    override fun close(handle: Long) = nativeClose(handle)

    companion object {
        @JvmStatic
        private external fun nativeOpen(screen: Int): Long

        @JvmStatic
        private external fun nativeWaitTick(handle: Long): Long

        @JvmStatic
        private external fun nativeClose(handle: Long)
    }
}

/**
 * The kernel DRM vblank of one CRTC; see `FramePacing.cc` in `cpp/linux`. Needs DRM device access,
 * which a local desktop session has through the logind seat ACL and a remote one does not.
 * [periodNanos] is the key the CRTC is matched on, so this clock is only built for a display whose
 * refresh rate the toolkit reported.
 */
internal class LinuxDrmVBlankClock(
    displayId: Long,
    periodNanos: Long
) : NativeVBlankClock(displayId, periodNanos) {

    override fun open(): Long = nativeOpen(periodNanos)

    override fun waitTick(handle: Long): Long = nativeWaitTick(handle)

    override fun close(handle: Long) = nativeClose(handle)

    companion object {
        fun available(): Boolean = nativeProbe()

        @JvmStatic
        private external fun nativeProbe(): Boolean

        @JvmStatic
        private external fun nativeOpen(displayPeriodNanos: Long): Long

        @JvmStatic
        private external fun nativeWaitTick(handle: Long): Long

        @JvmStatic
        private external fun nativeClose(handle: Long)
    }
}
