package org.jetbrains.skiko.swing

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.awt.GraphicsConfiguration
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.LockSupport

private typealias TickListener = (displayId: Long, timeNanos: Long) -> Unit

/**
 * A tick source for one display. It starts with its first listener and stops after its last one.
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

    /** @return true when removing [listener] stopped the clock. */
    fun remove(listener: TickListener): Boolean {
        listeners.remove(listener)
        if (listeners.isNotEmpty()) return false

        stopped = true
        onStop()
        return true
    }

    protected abstract fun onStart()
    protected abstract fun onStop()

    protected fun deliver(timeNanos: Long) {
        if (stopped) return
        for (listener in listeners) {
            try {
                listener(displayId, timeNanos)
            } catch (_: Throwable) {
                // One listener must not break the clock or other listeners.
            }
        }
    }
}

/**
 * The portable clock: a daemon thread that ticks on a phase-aligned deadline grid.
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
        thread?.let(LockSupport::unpark)
        thread = null
    }

    override fun run() {
        var deadline = System.nanoTime() + periodNanos
        while (!stopped) {
            val now = System.nanoTime()
            if (now < deadline) {
                LockSupport.parkNanos(deadline - now)
                continue
            }
            deadline += ((now - deadline) / periodNanos + 1) * periodNanos
            deliver(now)
        }
    }
}

/** A per-display registry of portable pacing clocks. */
internal class SkikoFramePacingService private constructor(
    private val newClock: (displayId: Long, periodNanos: Long) -> DisplayClock
) : FramePacingService {
    private val clocks = HashMap<Long, DisplayClock>()

    override fun displayId(graphicsConfiguration: GraphicsConfiguration): Long =
        deviceDisplayId(graphicsConfiguration.device)

    override fun refreshPeriodNanos(displayId: Long): Long {
        val rate = findDevice(displayId)?.displayMode?.refreshRate ?: return 0
        if (rate !in MIN_PLAUSIBLE_REFRESH_HZ..MAX_PLAUSIBLE_REFRESH_HZ) return 0
        return 1_000_000_000L / rate
    }

    @Synchronized
    override fun subscribe(displayId: Long, onTick: TickListener): AutoCloseable? {
        if (displayId == -1L || findDevice(displayId) == null) return null
        val clock = clocks.getOrPut(displayId) {
            val period = refreshPeriodNanos(displayId).takeIf { it > 0 } ?: FALLBACK_PERIOD_NANOS
            newClock(displayId, period)
        }
        clock.add(onTick)
        val closed = AtomicBoolean(false)
        return AutoCloseable {
            if (closed.compareAndSet(false, true)) unsubscribe(clock, onTick)
        }
    }

    @Synchronized
    private fun unsubscribe(clock: DisplayClock, listener: TickListener) {
        if (clock.remove(listener)) clocks.remove(clock.displayId, clock)
    }

    private fun findDevice(displayId: Long): GraphicsDevice? {
        if (displayId == -1L || GraphicsEnvironment.isHeadless()) return null
        return GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices.firstOrNull {
            it.type == GraphicsDevice.TYPE_RASTER_SCREEN && deviceDisplayId(it) == displayId
        }
    }

    companion object {
        private const val FALLBACK_PERIOD_NANOS = 1_000_000_000L / 60
        private const val MIN_PLAUSIBLE_REFRESH_HZ = 20
        private const val MAX_PLAUSIBLE_REFRESH_HZ = 1_000

        val instance: FramePacingService? by lazy {
            if (GraphicsEnvironment.isHeadless()) null
            else SkikoFramePacingService(::TimerClock)
        }

        internal fun deviceDisplayId(device: GraphicsDevice?): Long {
            if (device == null || device.type != GraphicsDevice.TYPE_RASTER_SCREEN) return -1
            val idString = device.iDstring ?: return -1
            val trailingDigits = idString.takeLastWhile { it.isDigit() }
            return when {
                hostOs == OS.MacOS || hostOs == OS.Windows ->
                    trailingDigits.toLongOrNull() ?: -1
                else -> idString.hashCode().toLong() and 0xFFFFFFFFL
            }
        }
    }
}
