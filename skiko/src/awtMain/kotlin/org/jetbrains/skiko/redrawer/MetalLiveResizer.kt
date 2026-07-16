package org.jetbrains.skiko.redrawer

import org.jetbrains.skiko.Library
import org.jetbrains.skiko.Logger
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.SwingUtilities.invokeLater

/**
 * Handles the live-resize rendering path for Metal.
 *
 * During an interactive window resize, AppKit drives the frame loop from the main thread so that
 * Metal content, drawable size, and the window backing are all committed in the same CATransaction.
 */
internal class MetalLiveResizer(
    private val vsyncRedrawer: MetalAWTRedrawer,
    windowHandle: Long,
) {
    companion object {
        init {
            Library.load()
        }
    }

    @Volatile
    var isInLiveResize: Boolean = false
        private set

    private var isDisposed = false
    private val ctxPtr: Long = create(windowHandle)

    private val frameScheduled = AtomicBoolean(false)

    private var width: Int = 0
    private var height: Int = 0

    fun dispose() {
        isDisposed = true
        dispose(ctxPtr)
    }

    private fun checkDisposed() {
        check(!isDisposed) { "${this.javaClass.simpleName} is disposed" }
    }

    fun needRender() {
        checkDisposed()
        if (frameScheduled.getAndSet(true)) return
        scheduleRenderFrameOnAppKitThread(ctxPtr)
    }

    fun onBoundsChangedInAppkitThread(width: Int, height: Int) {
        this@MetalLiveResizer.width = width
        this@MetalLiveResizer.height = height
        renderFrameInAppkitThread()
    }

    /**
     * Called from native code, on the AppKit main thread
     */
    private fun renderFrameInAppkitThread() {
        frameScheduled.set(false)
        val w = width
        val h = height
        if (!isInLiveResize || w <= 0 || h <= 0) return
        try {
            vsyncRedrawer.renderImmediatelyInAppKitThread(w, h)
        } catch (e: Exception) {
            Logger.warn(e) { "Failed to render live-resize frame" }
        }
    }

    /**
     * Called from native code, on the AppKit main thread, when a live resizing session starts.
     */
    @Suppress("unused")
    private fun onLiveResizeStartedInAppkitThread() {
        isInLiveResize = true
        vsyncRedrawer.setPresentsWithNativeTransaction(true)
    }

    /**
     * Called from native code, on the AppKit main thread, when a live resizing session ends.
     */
    @Suppress("unused")
    private fun onLiveResizeEndedInAppkitThread() {
        vsyncRedrawer.setPresentsWithNativeTransaction(false)
        isInLiveResize = false
        invokeLater {
            if (!isDisposed) {
                vsyncRedrawer.needRender(throttledToVsync = false)
            }
        }
    }

    private external fun create(windowPtr: Long): Long
    private external fun scheduleRenderFrameOnAppKitThread(ctxPtr: Long)
    private external fun dispose(ctxPtr: Long)
}
