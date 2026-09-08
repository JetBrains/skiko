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
    }

    private suspend fun awaitTick() {
        val service = service ?: return
        val graphicsConfiguration = component.graphicsConfiguration ?: return
        val displayId = service.displayId(graphicsConfiguration)
        val subscription = service.subscribe(displayId) { _, _ -> tickChannel.trySend(Unit) } ?: return
        try {
            withTimeout(DEFAULT_TICK_TIMEOUT_MILLIS) { tickChannel.receive() }
        } catch (_: TimeoutCancellationException) {
            // A failed clock only disables pacing for this frame in the MVP.
        } finally {
            subscription.close()
        }
    }

    private companion object {
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
