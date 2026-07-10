package org.jetbrains.skiko.redrawer

import org.jetbrains.skia.*
import org.jetbrains.skiko.*

internal class AngleRedrawer(
    host: AwtSurfaceHost,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties
) : AWTRedrawer(host, analytics, GraphicsApi.ANGLE) {
    init {
        try {
            loadAngleLibrary()
        } catch (e: Exception) {
            throw RenderException("Failed to load ANGLE library", cause = e)
        }
    }

    private var drawLock = Any()

    private var device: Long = 0L
        get() {
            if (field == 0L) {
                throw RenderException("ANGLE device is not initialized or already disposed")
            }
            return field
        }

    private val adapterName get() = AngleApi.glGetString(AngleApi.GL_RENDERER)

    override val directContext: DirectContext? get() = context

    init {
        device = host.backedLayer.useDrawingSurfacePlatformInfo { platformInfo ->
            createAngleDevice(platformInfo, host.transparency).takeIf { it != 0L }
                ?: throw RenderException("Failed to create ANGLE device.")
        }
        adapterName.let { adapterName ->
            if (adapterName != null && !isVideoCardSupported(GraphicsApi.ANGLE, hostOs, adapterName)) {
                throw RenderException("Cannot create ANGLE redrawer.")
            }
            onDeviceChosen(adapterName)
        }
        onContextInit()
    }

    // Only ever touched under `drawLock`.
    private var context: DirectContext? = null
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var canvas: Canvas? = null
    private var currentWidth = 0
    private var currentHeight = 0

    override val renderInfo: String
        get() = renderInfoHeader(host.renderApi) +
                "Vendor: ${AngleApi.glGetString(AngleApi.GL_VENDOR)}\n" +
                "Model: ${AngleApi.glGetString(AngleApi.GL_RENDERER)}\n" +
                "Version: ${AngleApi.glGetString(AngleApi.GL_VERSION)}\n"
                // "Total VRAM: ${AngleApi.glGetIntegerv(AngleApi.GL_TOTAL_MEMORY) / 1024} MB\n"

    override fun releaseResources() = synchronized(drawLock) {
        makeCurrent(device)
        disposeSurface()
        context?.close()
        context = null
        disposeDevice(device)
        device = 0L
    }

    override suspend fun renderFrame(scope: LayerDrawScope, immediate: Boolean) {
        val withVsync = if (immediate) SkikoProperties.windowsWaitForVsyncOnRedrawImmediately else properties.isVsyncEnabled
        drawAndSwap(scope, withVsync)
    }

    private fun drawAndSwap(scope: LayerDrawScope, withVsync: Boolean) = synchronized(drawLock) {
        // Re-check inside the lock (not just at the call site): this is what makes `dispose` and an
        // in-flight frame mutually exclusive rather than merely racing on `isDisposed`.
        if (isDisposed) {
            return
        }
        makeCurrent(device)
        with(scope) { drawFrame() }
        swapBuffers(device, withVsync)
    }

    override fun acquireSurface(width: Int, height: Int): Surface = synchronized(drawLock) {
        check(!isDisposed) { "AngleRedrawer is disposed" }
        makeCurrent(device)
        if (!ensureContext()) {
            throw RenderException("Cannot init graphic context")
        }
        createSurface(width, height, host.pixelGeometry)
        surface ?: throw RenderException("Cannot create surface for ${width}x$height")
    }

    override fun present() = synchronized(drawLock) {
        if (!isDisposed) {
            makeCurrent(device)
            context?.flush()
            swapBuffers(device, properties.isVsyncEnabled)
        }
    }

    private fun LayerDrawScope.drawFrame() {
        if (!ensureContext()) {
            throw RenderException("Cannot init graphic context")
        }
        initSurface()
        canvas?.runRestoringState {
            clear(Color.TRANSPARENT)
            host.draw(this)
        }
        context?.flush()
    }

    private fun ensureContext(): Boolean {
        if (context == null) {
            try {
                val newContext = DirectContext(
                    makeAngleContext(device).takeIf { it != 0L }
                        ?: throw RenderException("Failed to make GL context.")
                )
                context = newContext
                onContextInitialized(newContext, properties.gpuResourceCacheLimit) { renderInfo }
            } catch (e: Exception) {
                Logger.warn(e) { "Failed to create Skia ANGLE context!" }
                return false
            }
        }
        return true
    }

    private fun LayerDrawScope.initSurface() = createSurface(scaledLayerWidth, scaledLayerHeight, pixelGeometry)

    private fun createSurface(w: Int, h: Int, pixelGeometry: PixelGeometry) {
        val context = context ?: return

        if (isSizeChanged(w, h) || surface == null) {
            disposeSurface()
            context.flush()

            renderTarget = BackendRenderTarget(
                makeAngleRenderTarget(device, w, h).takeIf { it != 0L }
                    ?: throw RenderException("Failed to make ANGLE render target.")
            )
            surface = Surface.makeFromBackendRenderTarget(
                context,
                renderTarget!!,
                SurfaceOrigin.BOTTOM_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = pixelGeometry)
            ) ?: throw RenderException("Cannot create surface")
        }

        canvas = surface!!.canvas
    }

    private fun isSizeChanged(width: Int, height: Int): Boolean {
        if (width != currentWidth || height != currentHeight) {
            currentWidth = width
            currentHeight = height
            return true
        }
        return false
    }

    private fun disposeSurface() {
        surface?.close()
        renderTarget?.close()
        surface = null
        renderTarget = null
        canvas = null
    }
}

private external fun createAngleDevice(platformInfo: Long, transparency: Boolean): Long
private external fun makeCurrent(device: Long)
private external fun makeAngleContext(device: Long): Long
private external fun makeAngleRenderTarget(device: Long, width: Int, height: Int): Long
private external fun swapBuffers(device: Long, waitForVsync: Boolean)
private external fun disposeDevice(device: Long)
