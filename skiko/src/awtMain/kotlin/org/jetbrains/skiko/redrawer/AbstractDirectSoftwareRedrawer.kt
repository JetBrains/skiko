package org.jetbrains.skiko.redrawer

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.getPtr
import kotlinx.coroutines.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.layerFrameLimiter
import java.lang.ref.Reference

/**
 * The direct-software (CPU, blit-straight-to-window) on-screen render context ([AWTRedrawer]) shared by
 * Windows and Linux. It owns the native window-backed raster device (created by the platform subclass,
 * [WindowsSoftwareRedrawer] / [LinuxSoftwareRedrawer]) and the Skia raster [Surface] for the current frame.
 * The generic [OnScreenRedrawer] drives the frame loop. An optional software frame limiter runs in
 * [paceBeforeFrame].
 *
 * The platform subclass wraps the per-frame body and the native lifecycle calls in its drawing-surface lock
 * by overriding [draw]/[resize]/[finishFrame]/[dispose] (Linux); [renderFrame] and the synchronous
 * `renderImmediately` path both flow through [draw], so the subclass only needs to wrap it once.
 *
 * Content to draw is provided by [SkiaLayer.draw].
 */
internal abstract class AbstractDirectSoftwareRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : AWTRedrawer {

    /**
     * Guards the native device and the raster [surface]/[canvas]. Frame loop and [dispose] both run on the
     * EDT here, so they can't actually race; the lock is kept for uniformity with the off-EDT backends.
     */
    private val drawLock = Any()

    @Volatile
    private var isDisposed = false

    override val graphicsApi: GraphicsApi get() = GraphicsApi.SOFTWARE_FAST
    override val deviceName: String? get() = "Software"

    // Raster surface for the current frame, recreated on resize; only touched under `drawLock`.
    private var isContextInitialized = false
    private var surface: Surface? = null
    private var canvas: Canvas? = null
    private var currentWidth = 0
    private var currentHeight = 0

    override val renderInfo: String
        get() = renderInfoHeader(layer.renderApi)

    override fun isTransparentBackgroundSupported(): Boolean = defaultIsTransparentBackgroundSupported(layer)

    private val frameJob = Job()
    private val frameLimiter = layerFrameLimiter(CoroutineScope(frameJob), layer.backedLayer)

    override suspend fun paceBeforeFrame() {
        if (properties.isVsyncEnabled && properties.isVsyncFramelimitFallbackEnabled) {
            frameLimiter.awaitNextFrame()
        }
    }

    protected var device = 0L

    override suspend fun renderFrame(scope: LayerDrawScope, immediate: Boolean) = draw(scope)

    /**
     * Renders one frame. Kept `open` so the platform subclass ([LinuxSoftwareRedrawer]) can wrap the whole
     * body in its drawing-surface lock, mirroring the `draw()` override point.
     */
    protected open fun draw(scope: LayerDrawScope) = performDraw(scope)

    open fun resize(width: Int, height: Int) = resize(device, width, height)
    open fun finishFrame(surface: Long) = finishFrame(device, surface)

    override fun dispose() = synchronized(drawLock) {
        isDisposed = true
        frameJob.cancel()
        disposeSurface()
        disposeDevice(device)
    }

    private fun performDraw(scope: LayerDrawScope) = synchronized(drawLock) {
        if (!isDisposed) {
            with(scope) { drawFrame() }
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
