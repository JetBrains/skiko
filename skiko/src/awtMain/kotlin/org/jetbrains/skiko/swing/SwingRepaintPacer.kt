package org.jetbrains.skiko.swing

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout
import org.jetbrains.skiko.FrameDispatcher
import org.jetbrains.skiko.MainUIDispatcher
import java.awt.Component
import java.awt.GraphicsConfiguration

/**
 * The repaint entry point used by [SkiaSwingLayer.needRender].
 *
 * A requested frame is painted immediately, then the next request waits for a display tick.
 */
internal class SwingRepaintPacer(
    private val component: Component,
    private val service: FramePacingService? = FramePacingServices.default
) {
    private var disposed = false
    private var subscription: AutoCloseable? = null
    private var subscribedDisplayId = UNKNOWN_DISPLAY_ID
    private val tickChannel = Channel<Unit>(Channel.RENDEZVOUS)
    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (!disposed) {
            component.repaint()
            awaitTick()
        }
    }

    fun requestRepaint() {
        if (disposed || service == null) component.repaint()
        else frameDispatcher.scheduleFrame()
    }

    fun dispose() {
        disposed = true
        frameDispatcher.cancel()
        closeSubscription()
    }

    private suspend fun awaitTick() {
        val service = service ?: return
        if (!ensureSubscription(service)) return
        try {
            withTimeout(DEFAULT_TICK_TIMEOUT_MILLIS) { tickChannel.receive() }
        } catch (_: TimeoutCancellationException) {
            // A failed clock only disables pacing for this frame in the MVP.
        }
    }

    /** Keeps one display clock alive across a continuous animation. */
    private fun ensureSubscription(service: FramePacingService): Boolean {
        val displayId = resolveDisplayId(service)
        if (displayId == UNKNOWN_DISPLAY_ID) {
            closeSubscription()
            return false
        }
        if (subscription != null && displayId == subscribedDisplayId) return true

        closeSubscription()
        val newSubscription = service.subscribe(displayId) { _, _ -> onTick() } ?: return false
        subscription = newSubscription
        subscribedDisplayId = displayId
        return true
    }

    private fun onTick() {
        tickChannel.trySend(Unit)
    }

    /** Re-resolve on every frame so moving a window between displays replaces its clock. */
    private fun resolveDisplayId(service: FramePacingService): Long {
        val graphicsConfiguration = component.graphicsConfiguration ?: return UNKNOWN_DISPLAY_ID
        return service.displayId(graphicsConfiguration)
    }

    private fun closeSubscription() {
        subscription?.close()
        subscription = null
        subscribedDisplayId = UNKNOWN_DISPLAY_ID
    }

    private companion object {
        const val UNKNOWN_DISPLAY_ID = -1L
        const val DEFAULT_TICK_TIMEOUT_MILLIS = 50L
    }
}

/** The tick source [SwingRepaintPacer] consumes. */
internal interface FramePacingService {
    fun displayId(graphicsConfiguration: GraphicsConfiguration): Long
    fun refreshPeriodNanos(displayId: Long): Long
    fun subscribe(displayId: Long, onTick: (displayId: Long, timeNanos: Long) -> Unit): AutoCloseable?
}

internal object FramePacingServices {
    val default: FramePacingService? by lazy { SkikoFramePacingService.instance }
}
