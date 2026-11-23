package org.jetbrains.skiko.renderer

import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.MainUIDispatcher
import org.jetbrains.skiko.renderTime
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Decides when one window's scheduled frames run, producing each through the window's
 * [FrameProducer]. The [FrameDriver] requests the frames.
 */
internal interface FrameScheduler {
    /**
     * Schedules one frame; requests coalesce. `throttledToVsync = false` also asks for the early
     * content record on backends that pace after the frame.
     */
    fun scheduleFrame(throttledToVsync: Boolean)

    /** Holds scheduled frames while the platform drives the window's frames itself. */
    fun pause() {}

    fun resume() {}

    fun cancel() {}
}

/**
 * The default scheduler: a dispatcher per window, its frames paced by the backend's
 * [AwtRenderer.runFrame].
 */
internal class SingleFrameScheduler(private val producer: FrameProducer) : FrameScheduler {
    private val renderer get() = producer.renderer
    private val layer get() = producer.layer

    @Volatile
    private var isPaused = false

    private val updateRequested = AtomicBoolean(false)

    private fun requestUpdate() {
        updateRequested.set(true)
    }

    // Consumes the request, so the frame and early-record dispatchers record each frame's content once.
    private fun updateIfRequested() {
        if (updateRequested.getAndSet(false)) {
            producer.update(renderTime())
        }
    }

    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (!isPaused) {
            renderer.runFrame {
                if (layer.isShowing) {
                    updateIfRequested()
                    producer.drawFrame(immediate = false)
                }
            }
        }
    }

    // Records the next frame's content on the EDT while the previous frame still waits for vsync.
    private val earlyRecordDispatcher = if (!renderer.pacesAfterFrame) null else {
        FrameDispatcher(MainUIDispatcher) {
            if (layer.isShowing && !isPaused) updateIfRequested()
        }
    }

    override fun scheduleFrame(throttledToVsync: Boolean) {
        requestUpdate()
        if (!throttledToVsync) {
            earlyRecordDispatcher?.scheduleFrame()
        }
        frameDispatcher.scheduleFrame()
    }

    override fun pause() {
        isPaused = true
    }

    override fun resume() {
        isPaused = false
    }

    override fun cancel() {
        frameDispatcher.cancel()
        earlyRecordDispatcher?.cancel()
    }
}
