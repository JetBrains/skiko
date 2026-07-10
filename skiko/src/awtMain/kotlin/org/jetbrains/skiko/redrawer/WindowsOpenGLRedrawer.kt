package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.*

/**
 * The single per-window Windows (WGL) OpenGL on-screen render context ([AWTRedrawer]): it owns the
 * native WGL device/context lifecycle, the Skia [DirectContext] and on-screen GPU surface for the current
 * frame, and the present/swap. The frame loop itself lives in the generic [OnScreenRedrawer], which
 * drives this per-window render context.
 *
 * Pacing is **per window**: each frame draws + swaps + `glFinish`es on the EDT, then (if vsync is enabled)
 * waits once for [dwmFlush] off the EDT in [paceAfterFrame]. GL resource calls stay on the EDT, so (unlike
 * the GPU backends that hop off the EDT to render) this type does not need a `drawLock`: draw and [dispose]
 * never run concurrently. Only the [dwmFlush] vsync wait is moved off the EDT, and it touches no GL resource
 * that [dispose] frees.
 *
 * Content to draw is provided by [SkiaLayer.draw].
 */
internal class WindowsOpenGLRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : AWTRedrawer {
    init {
        loadOpenGLLibrary()
    }

    @Volatile
    private var isDisposed = false

    private val device: Long = layer.backedLayer.useDrawingSurfacePlatformInfo {
        getDevice(it).also { devicePtr ->
            check(devicePtr != 0L) { "Can't get device" }
        }
    }

    override val graphicsApi: GraphicsApi get() = GraphicsApi.OPENGL
    override var deviceName: String? = null
        private set

    private val context = createContext(device, layer.contentHandle, layer.transparency).also {
        if (it == 0L) {
            throw RenderException("Cannot create Windows GL context")
        }
        makeCurrent(device, it)
        adapterName.also { adapterName ->
            if (adapterName != null && !isVideoCardSupported(GraphicsApi.OPENGL, hostOs, adapterName)) {
                throw RenderException("Cannot create Windows GL context")
            }
            deviceName = adapterName
        }
    }

    private val adapterName get() = OpenGLApi.instance.glGetString(OpenGLApi.instance.GL_RENDERER)

    // GPU surface for the current frame.
    private var glContext: DirectContext? = null
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var canvas: Canvas? = null
    private var currentWidth = 0
    private var currentHeight = 0

    override val renderInfo: String
        get() {
            val gl = OpenGLApi.instance
            return renderInfoHeader(layer.renderApi) +
                    "Vendor: ${gl.glGetString(gl.GL_VENDOR)}\n" +
                    "Model: ${gl.glGetString(gl.GL_RENDERER)}\n" +
                    "Total VRAM: ${gl.glGetIntegerv(gl.GL_TOTAL_MEMORY) / 1024} MB\n"
        }

    override fun isTransparentBackgroundSupported(): Boolean = defaultIsTransparentBackgroundSupported(layer)

    init {
        makeCurrent()
        // For vsync we will use dwmFlush instead of swapInterval,
        // because it isn't reliable with DWM (Desktop Windows Manager): interval between frames isn't stable (14-19ms).
        // With dwmFlush it is stable (16.6-16.8 ms)
        // GLFW also uses dwmFlush (https://www.glfw.org/docs/3.0/window.html#window_swap)
        setSwapInterval(0)
    }

    override fun dispose() {
        check(!isDisposed) { "WindowsOpenGLRedrawer is disposed" }
        isDisposed = true
        makeCurrent()
        disposeSurface()
        glContext?.close()
        glContext = null
        deleteContext(context)
    }

    override suspend fun renderFrame(scope: LayerDrawScope, immediate: Boolean) {
        makeCurrent()
        with(scope) { drawFrame() }
        swapBuffers()
        OpenGLApi.instance.glFinish()
        if (immediate && SkikoProperties.windowsWaitForVsyncOnRedrawImmediately) {
            // The looped path waits for vsync off the EDT in paceAfterFrame; the immediate path waits inline.
            dwmFlush()
        }
    }

    override suspend fun paceAfterFrame() {
        if (properties.isVsyncEnabled) {
            withContext(dispatcherToBlockOn) {
                dwmFlush() // wait for vsync
            }
        }
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
        glContext?.flush()
    }

    private fun ensureContext(): Boolean {
        if (glContext == null) {
            try {
                val newContext = makeGLContext()
                glContext = newContext
                onContextInitialized(newContext, layer.properties.gpuResourceCacheLimit) { renderInfo }
            } catch (e: Exception) {
                Logger.warn(e) { "Failed to create Skia OpenGL context!" }
                return false
            }
        }
        return true
    }

    private fun LayerDrawScope.initSurface() {
        val glContext = glContext ?: return

        val w = scaledLayerWidth
        val h = scaledLayerHeight

        if (isSizeChanged(w, h) || surface == null) {
            disposeSurface()
            val gl = OpenGLApi.instance
            val fbId = gl.glGetIntegerv(gl.GL_DRAW_FRAMEBUFFER_BINDING)
            renderTarget = makeGLRenderTarget(
                w,
                h,
                0,
                8,
                fbId,
                FramebufferFormat.GR_GL_RGBA8
            )
            surface = Surface.makeFromBackendRenderTarget(
                glContext,
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

    private fun makeCurrent() = makeCurrent(device, context)
    private fun swapBuffers() = swapBuffers(device)
}

private external fun makeCurrent(device: Long, context: Long)
private external fun getDevice(platformInfo: Long): Long
private external fun createContext(device: Long, contentHandle:Long, transparency: Boolean): Long
private external fun deleteContext(context: Long)
private external fun setSwapInterval(interval: Int)
private external fun swapBuffers(device: Long)

// TODO according to https://bugs.chromium.org/p/chromium/issues/detail?id=467617 dwmFlush has lag 3 ms after vsync.
//  Maybe we should use D3DKMTWaitForVerticalBlankEvent? See also https://www.vsynctester.com/chromeisbroken.html
// TODO should we support Windows 7? DWM can be disabled on Windows 7.
//  it that case there will be a crash or just no frame limit (I don't know exactly).
private external fun dwmFlush()
