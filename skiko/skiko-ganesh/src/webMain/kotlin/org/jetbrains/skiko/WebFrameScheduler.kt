package org.jetbrains.skiko

import kotlin.math.ceil

/** rAF by default; opt-in pacing uses timer -> MessageChannel tasks on a vsync phase grid. */
internal class WebFrameScheduler(
    targetFrameRate: Double? = null,
    private val clock: WebFrameClock = BrowserFrameClock(),
    private val drawFrame: (Double) -> Unit
) {
    init {
        require(targetFrameRate == null || targetFrameRate.isFinite() && targetFrameRate in 1.0..240.0)
    }

    private val period = targetFrameRate?.let { 1000.0 / it }
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
        } else stop()
    }

    init {
        if (period != null) clock.addVisibilityListener(visibilityChanged)
    }

    private val animationCallback: (Double) -> Unit = { timestamp ->
        raf = null
        if (!disposed) {
            if (period == null) {
                requested = false
                drawFrame(timestamp)
            } else if (requested && clock.isVisible()) {
                // Safari flushes scroll positions on rendering updates. Keep this loop
                // active only while frames are requested, and use its timestamp as phase.
                phase = timestamp
                raf = clock.requestAnimationFrame(animationCallbackRef())
            }
        }
    }

    private fun animationCallbackRef() = animationCallback

    fun requestFrame() {
        if (disposed || requested) return
        requested = true
        schedule()
    }

    private fun schedule() {
        if (period == null) {
            raf = clock.requestAnimationFrame(animationCallback)
            return
        }
        if (!clock.isVisible()) return
        clock.setHighRefreshRateActive(true)
        if (raf == null) raf = clock.requestAnimationFrame(animationCallback)
        val now = clock.now()
        // performance.now() can be rounded to milliseconds on iOS. Derive the
        // minimum spacing from the refresh interval, not a hardcoded 8.33 ms gate.
        val earliest = maxOf(now, lastDraw + period * 0.8)
        deadline = if (phase.isNaN()) maxOf(now, lastDraw + period)
        else phase + ceil((earliest - phase) / period) * period
        scheduleTimer(maxOf(0.0, deadline - now))
    }

    private fun scheduleTimer(delay: Double) {
        timer = clock.setTimeout({
            timer = null
            task = clock.postTask {
                task = null
                tick()
            }
        }, delay)
    }

    private fun tick() {
        if (disposed || !requested || !clock.isVisible()) return
        val now = clock.now()
        val interval = period!!
        val remaining = maxOf(deadline - now - interval * 0.05, lastDraw + interval * 0.8 - now)
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
        if (period != null) clock.removeVisibilityListener(visibilityChanged)
        clock.close()
    }
}

internal interface WebFrameClock {
    fun now(): Double
    fun requestAnimationFrame(callback: (Double) -> Unit): Int
    fun cancelAnimationFrame(id: Int)
    fun setTimeout(callback: () -> Unit, delay: Double): Int
    fun cancelTimeout(id: Int)
    fun postTask(callback: () -> Unit): Int
    fun cancelTask(id: Int)
    fun isVisible(): Boolean
    fun addVisibilityListener(callback: () -> Unit)
    fun removeVisibilityListener(callback: () -> Unit)
    fun setHighRefreshRateActive(active: Boolean)
    fun close()
}
