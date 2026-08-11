package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skiko.*
import org.jetbrains.skiko.context.ContextFreeContextHandler
import org.jetbrains.skiko.layerFrameLimiter
import java.lang.ref.Reference

internal abstract class AbstractDirectSoftwareRedrawer(
    layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    properties: SkiaLayerProperties
) : ContextFreeContextHandler(layer, analytics, GraphicsApi.SOFTWARE_FAST) {
    private val frameJob = Job()
    private val frameLimiter = layerFrameLimiter(CoroutineScope(frameJob), layer.backedLayer)
    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (properties.isVsyncEnabled && properties.isVsyncFramelimitFallbackEnabled) {
            frameLimiter.awaitNextFrame()
        }

        if (layer.isShowing) {
            update()
            drawFrame()
        }
    }

    protected var device = 0L

    private var currentWidth = 0
    private var currentHeight = 0
    private fun isSizeChanged(width: Int, height: Int): Boolean {
        if (width != currentWidth || height != currentHeight) {
            currentWidth = width
            currentHeight = height
            return true
        }
        return false
    }

    override fun needRender(throttledToVsync: Boolean) {
        frameDispatcher.scheduleFrame()
    }

    protected open fun drawFrame() = inDrawScope { draw() }

    override fun renderImmediately() {
        update()
        if (!isDisposed) { // Redrawer may be disposed in user code, during `update`
            drawFrame()
        }
    }

    open fun resize(width: Int, height: Int) = resize(device, width, height)

    private fun acquireSurface(): Surface {
        val surface = acquireSurface(device)
        if (surface == 0L) {
            throw RenderException("Failed to create Surface")
        }
        return Surface(surface)
    }

    open fun finishFrame(surface: Long) = finishFrame(device, surface)

    override fun dispose() {
        frameJob.cancel()
        frameDispatcher.cancel()
        super.dispose()
        disposeDevice(device)
    }

    override fun LayerDrawScope.initCanvas() {
        val w = scaledLayerWidth
        val h = scaledLayerHeight
        if (isSizeChanged(w, h) || surface == null) {
            disposeCanvas()
            if (w > 0 && h > 0) {
                resize(w, h)
                surface = acquireSurface()
                canvas = surface!!.canvas
            } else {
                surface = null
                canvas = null
            }
        }
    }

    override fun flush() {
        val surface = surface
        if (surface != null) {
            try {
                finishFrame(getPtr(surface))
            } finally {
                Reference.reachabilityFence(surface)
            }
        }
    }

    private external fun resize(devicePtr: Long, width: Int, height: Int)
    private external fun acquireSurface(devicePtr: Long): Long
    private external fun finishFrame(devicePtr: Long, surfacePtr: Long)
    private external fun disposeDevice(devicePtr: Long)
}