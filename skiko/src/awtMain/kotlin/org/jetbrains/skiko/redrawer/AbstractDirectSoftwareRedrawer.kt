package org.jetbrains.skiko.redrawer

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.getPtr
import kotlinx.coroutines.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.layerFrameLimiter
import java.lang.ref.Reference

internal abstract class AbstractDirectSoftwareRedrawer(
    private val layer: SkiaLayer,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AWTRedrawer(layer, analytics, GraphicsApi.SOFTWARE_FAST) {

    private var isContextInitialized = false
    private var surface: Surface? = null
    private var canvas: Canvas? = null
    private var currentWidth = 0
    private var currentHeight = 0

    override val renderInfo: String
        get() = renderInfoHeader(layer.renderApi)

    private val frameJob = Job()
    private val frameLimiter = layerFrameLimiter(CoroutineScope(frameJob), layer.backedLayer)
    private val frameDispatcher = FrameDispatcher(MainUIDispatcher) {
        if (properties.isVsyncEnabled && properties.isVsyncFramelimitFallbackEnabled) {
            frameLimiter.awaitNextFrame()
        }

        if (layer.isShowing) {
            update()
            draw()
        }
    }

    protected var device = 0L

    override fun needRender(throttledToVsync: Boolean) {
        frameDispatcher.scheduleFrame()
    }

    protected open fun draw() = inDrawScope { performDraw() }

    override fun renderImmediately() {
        update()
        if (!isDisposed) { // Redrawer may be disposed in user code, during `update`
            draw()
        }
    }

    open fun resize(width: Int, height: Int) = resize(device, width, height)
    open fun finishFrame(surface: Long) = finishFrame(device, surface)

    override fun dispose() {
        frameJob.cancel()
        frameDispatcher.cancel()
        disposeSurface()
        disposeDevice(device)
        super.dispose()
    }

    private fun LayerDrawScope.performDraw() {
        if (!isDisposed) {
            drawFrame()
        }
    }

    private fun LayerDrawScope.drawFrame() {
        if (!ensureContext()) {
            throw RenderException("Cannot init graphic context")
        }
        initCanvas()
        canvas?.runRestoringState {
            clear(Color.TRANSPARENT)
            layer.draw(this)
        }
        flushFrame()
    }

    private fun ensureContext(): Boolean {
        if (!isContextInitialized) {
            isContextInitialized = true
            logRendererInfo { renderInfo }
        }
        return isContextInitialized
    }

    private fun LayerDrawScope.initCanvas() {
        val w = scaledLayerWidth
        val h = scaledLayerHeight
        if (isSizeChanged(w, h) || surface == null) {
            disposeSurface()
            if (w > 0 && h > 0) {
                resize(w, h)
                val surfacePtr = acquireSurface(device)
                if (surfacePtr == 0L) {
                    throw RenderException("Failed to create Surface")
                }
                surface = Surface(surfacePtr)
                canvas = surface!!.canvas
            }
        }
    }

    private fun isSizeChanged(width: Int, height: Int): Boolean {
        if (width != currentWidth || height != currentHeight) {
            currentWidth = width
            currentHeight = height
            return true
        }
        return false
    }

    private fun flushFrame() {
        val surface = surface ?: return
        try {
            finishFrame(getPtr(surface))
        } finally {
            Reference.reachabilityFence(surface)
        }
    }

    private fun disposeSurface() {
        surface?.close()
        surface = null
        canvas = null
    }

    private external fun resize(devicePtr: Long, width: Int, height: Int)
    private external fun acquireSurface(devicePtr: Long): Long
    private external fun finishFrame(devicePtr: Long, surfacePtr: Long)
    private external fun disposeDevice(devicePtr: Long)
}