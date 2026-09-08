package org.jetbrains.skiko.swing

import java.util.concurrent.CopyOnWriteArrayList
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
