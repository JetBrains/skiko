package org.jetbrains.skiko.redrawer

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.getPtr
import kotlinx.coroutines.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.layerFrameLimiter
import java.lang.ref.Reference

internal abstract class AbstractDirectSoftwareRedrawer(
    host: AwtSurfaceHost,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AWTRedrawer(host, analytics, GraphicsApi.SOFTWARE_FAST) {

    /** [acquireSurface] and [present] are public API, so unlike the frame loop they are not EDT-confined. */
    private val drawLock = Any()

    // Direct software rasterizes on the CPU into a native window-backed raster surface: no Ganesh DirectContext.
    override val directContext: DirectContext? get() = null

    // Only ever touched under `drawLock`.
    private var isContextInitialized = false
    private var surface: Surface? = null
    private var canvas: Canvas? = null
    private var currentWidth = 0
    private var currentHeight = 0

    override val renderInfo: String
        get() = renderInfoHeader(host.renderApi)

    private val frameJob = Job()
    private val frameLimiter = layerFrameLimiter(CoroutineScope(frameJob), host.backedLayer)

    override suspend fun runFrame(frame: suspend () -> Unit) {
        if (properties.isVsyncEnabled && properties.isVsyncFramelimitFallbackEnabled) {
            frameLimiter.awaitNextFrame()
        }
        frame()
    }

    protected var device = 0L

    override suspend fun renderFrame(scope: LayerDrawScope, immediate: Boolean) = draw(scope)

    override fun acquireSurface(width: Int, height: Int): Surface = synchronized(drawLock) {
        check(!isDisposed) { "DirectSoftwareRedrawer is disposed" }
        ensureContext()
        createSurface(width, height)
        surface ?: throw RenderException("Cannot create surface for ${width}x$height")
    }

    override fun present() = synchronized(drawLock) {
        if (!isDisposed) {
            flushFrame()
        }
    }

    protected open fun draw(scope: LayerDrawScope) = performDraw(scope)

    open fun resize(width: Int, height: Int) = resize(device, width, height)
    open fun finishFrame(surface: Long) = finishFrame(device, surface)

    override fun releaseResources() {
        frameJob.cancel()
        disposeSurface()
        disposeDevice(device)
    }

    private fun performDraw(scope: LayerDrawScope) = synchronized(drawLock) {
        // Re-check inside the lock (not just at the call site), matching MetalRedrawer/SoftwareRedrawer:
        // this is what makes `dispose` and an in-flight frame mutually exclusive.
        if (!isDisposed) {
            with(scope) { drawFrame() }
        }
    }

    private fun LayerDrawScope.drawFrame() {
        ensureContext()
        initCanvas()
        canvas?.runRestoringState {
            clear(Color.TRANSPARENT)
            host.draw(this)
        }
        flushFrame()
    }

    private fun ensureContext() {
        if (!isContextInitialized) {
            isContextInitialized = true
            logRendererInfo { renderInfo }
        }
    }

    private fun LayerDrawScope.initCanvas() = createSurface(scaledLayerWidth, scaledLayerHeight)

    private fun createSurface(w: Int, h: Int) {
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