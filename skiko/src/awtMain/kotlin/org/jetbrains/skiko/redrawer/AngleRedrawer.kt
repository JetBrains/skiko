package org.jetbrains.skiko.redrawer

import org.jetbrains.skia.*
import org.jetbrains.skiko.*

/**
 * The single per-window ANGLE (EGL-over-D3D11) on-screen render context ([AWTRedrawer]): it owns the
 * native ANGLE device lifecycle, the Skia [DirectContext] and on-screen GPU surface for the current frame,
 * and the present/swap (with vsync in the swap). The frame loop itself lives in the generic
 * [OnScreenRedrawer].
 *
 * The class name is bound to its JNI symbols, including the file facade
 * `Java_org_jetbrains_skiko_redrawer_AngleRedrawerKt_*` for the top-level `external fun`s; renaming it alone
 * unbinds them (UnsatisfiedLinkError at runtime, not a compile error).
 *
 * Content to draw is provided by [SkiaLayer.draw].
 *
 * @see "src/awtMain/cpp/windows/AngleRedrawer.cc" -- native implementation
 */
internal class AngleRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : AWTRedrawer {
    init {
        try {
            loadAngleLibrary()
        } catch (e: Exception) {
            throw RenderException("Failed to load ANGLE library", cause = e)
        }
    }

    /**
     * Guards every native touch point; [dispose] and the frame path ([drawAndSwap]) both take it and
     * re-check [isDisposed] inside, matching the other backends' discipline.
     */
    private val drawLock = Any()

    @Volatile
    private var isDisposed = false

    private var device: Long = 0L
        get() {
            if (field == 0L) {
                throw RenderException("ANGLE device is not initialized or already disposed")
            }
            return field
        }

    private val adapterName get() = AngleApi.glGetString(AngleApi.GL_RENDERER)

    override val graphicsApi: GraphicsApi get() = GraphicsApi.ANGLE
    override val deviceName: String?

    init {
        device = layer.backedLayer.useDrawingSurfacePlatformInfo { platformInfo ->
            createAngleDevice(platformInfo, layer.transparency).takeIf { it != 0L }
                ?: throw RenderException("Failed to create ANGLE device.")
        }
        deviceName = adapterName.also { adapterName ->
            if (adapterName != null && !isVideoCardSupported(GraphicsApi.ANGLE, hostOs, adapterName)) {
                throw RenderException("Cannot create ANGLE redrawer.")
            }
        }
    }

    // GPU surface for the current frame; only touched under `drawLock`.
    private var context: DirectContext? = null
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var canvas: Canvas? = null
    private var currentWidth = 0
    private var currentHeight = 0

    override val renderInfo: String
        get() = renderInfoHeader(layer.renderApi) +
                "Vendor: ${AngleApi.glGetString(AngleApi.GL_VENDOR)}\n" +
                "Model: ${AngleApi.glGetString(AngleApi.GL_RENDERER)}\n" +
                "Version: ${AngleApi.glGetString(AngleApi.GL_VERSION)}\n"

    override fun isTransparentBackgroundSupported(): Boolean = defaultIsTransparentBackgroundSupported(layer)

    override fun dispose() = synchronized(drawLock) {
        isDisposed = true
        makeCurrent(device)
        disposeSurface()
        context?.close()
        context = null
        disposeDevice(device)
        device = 0L
    }

    override suspend fun renderFrame(scope: LayerDrawScope, immediate: Boolean) {
        // ANGLE renders on the EDT (there is no off-EDT hand-off); vsync is applied inside the swap.
        val withVsync = if (immediate) SkikoProperties.windowsWaitForVsyncOnRedrawImmediately else properties.isVsyncEnabled
        drawAndSwap(scope, withVsync)
    }

    private fun drawAndSwap(scope: LayerDrawScope, withVsync: Boolean) = synchronized(drawLock) {
        if (isDisposed) {
            return
        }
        makeCurrent(device)
        with(scope) { drawFrame() }
        swapBuffers(device, withVsync)
    }

    private fun LayerDrawScope.drawFrame() {
        if (!ensureContext()) {
            throw RenderException("Cannot init graphic context")
        }
        initSurface()
        canvas?.runRestoringState {
            clear(Color.TRANSPARENT)
            layer.draw(this)
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
                onContextInitialized(newContext, layer.properties.gpuResourceCacheLimit) { renderInfo }
            } catch (e: Exception) {
                Logger.warn(e) { "Failed to create Skia ANGLE context!" }
                return false
            }
        }
        return true
    }

    private fun LayerDrawScope.initSurface() {
        val context = context ?: return

        val w = scaledLayerWidth
        val h = scaledLayerHeight

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
