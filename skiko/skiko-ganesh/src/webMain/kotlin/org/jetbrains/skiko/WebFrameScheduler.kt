package org.jetbrains.skiko

import kotlin.math.ceil

/** Uses rAF by default, or timer/MessageChannel tasks aligned to rAF for the 120 Hz experiment. */
internal class WebFrameScheduler(
    private val highRefreshRate: Boolean = false,
    private val clock: WebFrameClock = BrowserFrameClock(),
    private val drawFrame: (Double) -> Unit
) {
    private var requested = false
    private var disposed = false
    private var timer: Int? = null
    private var task: Int? = null
    private var raf: Int? = null
    private var phase = Double.NaN
    private var lastDraw = Double.NEGATIVE_INFINITY
    private var deadline = 0.0
    private val visibilityChanged: () -> Unit = {
        if (clock.isVisible()) {
            phase = Double.NaN
            lastDraw = Double.NEGATIVE_INFINITY
            if (requested) schedule()
        } else {
            stop()
        }
    }

    init {
        if (highRefreshRate) clock.setVisibilityListener(visibilityChanged)
    }

    private val animationCallback: (Double) -> Unit = { onAnimationFrame(it) }

    private fun onAnimationFrame(timestamp: Double) {
        raf = null
        if (disposed) return
        if (!highRefreshRate) {
            requested = false
            drawFrame(timestamp)
        } else if (requested && clock.isVisible()) {
            // Queued rendering updates make Safari flush scroll positions without waiting for IPC acknowledgements.
            phase = timestamp
            raf = clock.requestAnimationFrame(animationCallback)
        }
    }

    fun requestFrame() {
        if (disposed || requested) return
        requested = true
        schedule()
    }

    private fun schedule() {
        if (!highRefreshRate) {
            raf = clock.requestAnimationFrame(animationCallback)
            return
        }
        if (!clock.isVisible()) return
        clock.setHighRefreshRateActive(true)
        if (raf == null) raf = clock.requestAnimationFrame(animationCallback)
        val now = clock.now()
        val earliest = maxOf(now, lastDraw + MIN_FRAME_SPACING)
        // A 60 Hz rAF timestamp anchors two 120 Hz frame slots.
        deadline = if (phase.isNaN()) {
            maxOf(now, lastDraw + FRAME_INTERVAL)
        } else {
            phase + ceil((earliest - phase) / FRAME_INTERVAL) * FRAME_INTERVAL
        }
        scheduleTimer(maxOf(0.0, deadline - now))
    }

    private fun scheduleTimer(delay: Double) {
        timer = clock.setTimeout({
            timer = null
            // Yield through a message task so nested timers do not accumulate browser timer clamping.
            task = clock.postTask {
                task = null
                tick()
            }
        }, delay)
    }

    private fun tick() {
        if (disposed || !requested || !clock.isVisible()) return
        val now = clock.now()
        val remaining = maxOf(deadline - now - DEADLINE_TOLERANCE, lastDraw + MIN_FRAME_SPACING - now)
        if (remaining > 0) {
            scheduleTimer(remaining)
            return
        }
        lastDraw = now
        requested = false
        try {
            drawFrame(now)
        } finally {
            if (!requested) stop()
        }
    }

    private fun stop() {
        timer?.let(clock::cancelTimeout)
        task?.let(clock::cancelTask)
        raf?.let(clock::cancelAnimationFrame)
        timer = null
        task = null
        raf = null
        clock.setHighRefreshRateActive(false)
    }

    fun dispose() {
        if (disposed) return
        disposed = true
        requested = false
        stop()
        clock.close()
    }
}

private const val FRAME_INTERVAL = 1000.0 / 120
// Leave room for iOS's millisecond clock rounding while preventing duplicate draws near a deadline.
private const val MIN_FRAME_SPACING = FRAME_INTERVAL * 0.8
private const val DEADLINE_TOLERANCE = FRAME_INTERVAL * 0.05

internal interface WebFrameClock {
    fun now(): Double
    fun requestAnimationFrame(callback: (Double) -> Unit): Int
    fun cancelAnimationFrame(id: Int)
    fun setTimeout(callback: () -> Unit, delay: Double): Int
    fun cancelTimeout(id: Int)
    fun postTask(callback: () -> Unit): Int
    fun cancelTask(id: Int)
    fun isVisible(): Boolean
    fun setVisibilityListener(callback: (() -> Unit)?)
    fun setHighRefreshRateActive(active: Boolean)
    fun close()
}
