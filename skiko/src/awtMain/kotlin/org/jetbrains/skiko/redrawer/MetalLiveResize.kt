package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.SkiaLayer
import java.awt.Component
import java.awt.Dimension

/**
 * Owns the interactive (edge-drag) live-resize path for [MetalRedrawer].
 *
 * While the window is being resized, frames are driven from the AppKit main thread (instead of the
 * background frame loop) so that each frame's present joins the same [CATransaction] that commits the
 * window's new size — this is what keeps content and backing in sync and avoids white bars. This class
 * holds the live-resize state, the JNI bridge that schedules those frames and hops to the AWT event
 * thread, and the native upcall entry points; [MetalRedrawer] delegates the actual per-frame draw work
 * back to itself via a small internal contract.
 *
 * Native counterpart: `MetalRedrawer.mm` (the live-resize observers, `AWTMetalLayer.setBounds`, and the
 * `scheduleFrameOnAppKitThread` / `invokeOnEventThreadAndWait` implementations).
 */
internal class MetalLiveResize(
    private val redrawer: MetalRedrawer,
    private val layer: SkiaLayer,
) {
    /**
     * Whether the window is in live-resize mode.
     */
    @Volatile
    var isActive: Boolean = false
        private set

    /**
     * Called from native code, on the AppKit main thread, when a live resizing session starts.
     */
    @Suppress("unused")
    fun onLiveResizeStarted() {
        isActive = true
    }

    /**
     * Called from native code, on the AppKit main thread, when a live resizing session ends.
     */
    @Suppress("unused")
    fun onLiveResizeEnded() {
        isActive = false
        redrawer.requestRenderAfterLiveResize()
    }

    /**
     * Called from native code, on the AppKit main thread, to draw a frame during live resize.
     */
    @Suppress("unused")
    fun drawFrameWhileLiveResizing(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return

        // Record content at exactly the present size, on the EDT.
        try {
            invokeOnEventThreadAndWait {
                val size = Dimension(width, height)
                with(redrawer) {
                    update(forcedSize = size)
                    inDrawScope(forcedSize = size) {
                        if (!isDisposed) {
                            performDraw(finishFrame = false)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Logger.warn(e) { "Failed to record live-resize frame" }
            return
        }

        // The present must run on the AppKit main thread to join the resize transaction
        with(redrawer) {
            synchronized(drawLock) {
                if (!isDisposed) {
                    contextHandler.finishFrameInLiveResize()
                }
            }
        }
    }

    /**
     * Requests one frame on the AppKit main thread (during a live resize).
     *
     * Coalescing (at most one pending frame) lives natively in `scheduleFrameOnAppKitThread`, which hops
     * to the main queue and calls back into [drawFrameWhileLiveResizing], where the frame is rendered and
     * presented.
     */
    fun scheduleFrame() {
        val devicePtr = redrawer.liveResizeDevicePtr ?: return
        scheduleFrameOnAppKitThread(devicePtr)
    }

    /**
     * Hops to the AppKit main thread (main dispatch queue) and calls back into [drawFrameWhileLiveResizing]
     * with the layer's current pixel size. Used to drive frames through the single main-thread presenter
     * during a live resize.
     */
    private external fun scheduleFrameOnAppKitThread(device: Long)

    /**
     * Like [javax.swing.SwingUtilities.invokeAndWait], but keeps the AppKit run loop spinning while waiting, so
     * synchronous Java->AppKit calls made from [runnable] are serviced rather than deadlocking.
     * [component] provides the AWT context for the call.
     *
     * This is done via `LWCToolkit.invokeAndWait`. `LWCToolkit` lives in a non-exported JDK package, but JNI is not
     * subject to module access checks, so this needs no `--add-opens`.
     */
    private external fun invokeOnEventThreadAndWait(runnable: Runnable, component: Component)

    private fun invokeOnEventThreadAndWait(runnable: Runnable) {
        invokeOnEventThreadAndWait(runnable, layer)
    }
}
