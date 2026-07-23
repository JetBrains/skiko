package org.jetbrains.skiko.redrawer

import kotlinx.coroutines.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.*

/**
 * The single per-window Linux (GLX) OpenGL on-screen render context ([AWTRedrawer]): it owns the native
 * GLX context lifecycle (created from the window's X11 display), the Skia [DirectContext] and on-screen GPU
 * surface for the current frame, and the present/swap. The frame loop itself lives in the generic
 * [OnScreenRedrawer].
 *
 * Pacing is per window: every GLX call (create / make-current / swap / swap-interval / destroy) runs inside
 * [org.jetbrains.skiko.lockLinuxDrawingSurface], and [renderFrame] moves the *entire* per-frame body (lock,
 * make current, draw, swap, unlock) onto [dispatcherToBlockOn] rather than just the vsync wait, since GLX has
 * no decoupled vsync-wait primitive like Windows' `dwmFlush` -- the vblank wait is fused into
 * `glXSwapBuffers` itself. An additional software frame limiter runs in [paceBeforeFrame] (some Linuxes don't
 * honour vsync).
 *
 * The class name is bound to its JNI symbols, including the file facade
 * `Java_org_jetbrains_skiko_redrawer_LinuxOpenGLRedrawerKt_*` for the top-level `external fun`s; renaming it
 * alone unbinds them (UnsatisfiedLinkError at runtime, not a compile error).
 *
 * Content to draw is provided by [SkiaLayer.draw].
 */
internal class LinuxOpenGLRedrawer(
    private val layer: SkiaLayer,
    private val properties: SkiaLayerProperties
) : AWTRedrawer {
    init {
        loadOpenGLLibrary()
    }

    /**
     * Guards every native touch point. Frames render off the EDT (see [renderFrame]) while [dispose] can run
     * on the EDT concurrently; both take this lock and re-check [isDisposed] inside it, so [dispose] can't
     * free the GLX context out from under an in-flight JNI call.
     */
    private val drawLock = Any()

    @Volatile
    private var isDisposed = false

    private var context = 0L
    private val swapInterval = if (properties.isVsyncEnabled) 1 else 0

    override val graphicsApi: GraphicsApi get() = GraphicsApi.OPENGL
    override val deviceName: String?

    init {
        var name: String? = null
        layer.backedLayer.lockLinuxDrawingSurface {
            context = it.createContext(layer.transparency)
            if (context == 0L) {
                throw RenderException("Cannot create Linux GL context")
            }
            it.makeCurrent(context)
            name = adapterName.also { adapterName ->
                if (adapterName != null && !isVideoCardSupported(GraphicsApi.OPENGL, hostOs, adapterName)) {
                    throw RenderException("Cannot create Linux GL context")
                }
            }
            it.setSwapInterval(swapInterval)
        }
        deviceName = name
    }

    private val adapterName get() = OpenGLApi.instance.glGetString(OpenGLApi.instance.GL_RENDERER)

    // GPU surface for the current frame; only touched under `drawLock`.
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

    private val frameJob = Job()
    @Volatile
    private var frameLimit = 0.0
    private val frameLimiter = layerFrameLimiter(
        CoroutineScope(frameJob),
        layer.backedLayer,
        onNewFrameLimit = { frameLimit = it }
    )

    private suspend fun limitFramesIfNeeded() {
        // Some Linuxes don't turn vsync on, so we apply additional frame limit (which should be no longer than enabled vsync)
        if (properties.isVsyncEnabled) {
            try {
                frameLimiter.awaitNextFrame()
            } catch (e: CancellationException) {
                // ignore
            }
        }
    }

    override suspend fun paceBeforeFrame() {
        // Gate the software frame limiter on visibility: pace only while the window is showing.
        if (layer.isShowing) {
            limitFramesIfNeeded()
        }
    }

    override fun dispose() {
        isDisposed = true
        frameJob.cancel()
        synchronized(drawLock) {
            layer.backedLayer.lockLinuxDrawingSurface {
                // makeCurrent is mandatory to destroy context, otherwise, OpenGL will destroy wrong context (from another window).
                // see the official example: https://www.khronos.org/opengl/wiki/Tutorial:_OpenGL_3.0_Context_Creation_(GLX)
                it.makeCurrent(context)
                disposeSurface()
                glContext?.close()
                glContext = null
                it.destroyContext(context)
            }
        }
    }

    override suspend fun renderFrame(scope: LayerDrawScope, immediate: Boolean) {
        if (immediate) {
            val turnOffVsync = properties.isVsyncEnabled && !SkikoProperties.linuxWaitForVsyncOnRedrawImmediately
            drawAndSwap(scope, turnOffVsync)
        } else {
            // Move the entire per-frame body (JAWT lock, make current, draw, swap, unlock) off the EDT: see
            // the class kdoc for why this can't be limited to just the swap call the way Windows does it
            // with dwmFlush.
            withContext(dispatcherToBlockOn) {
                drawAndSwap(scope, turnOffVsync = false)
            }
        }
    }

    private fun drawAndSwap(scope: LayerDrawScope, turnOffVsync: Boolean) = synchronized(drawLock) {
        if (isDisposed) {
            return
        }
        layer.backedLayer.lockLinuxDrawingSurface {
            it.makeCurrent(context)
            with(scope) { drawFrame() }
            if (turnOffVsync) {
                it.setSwapInterval(0)
            }
            it.swapBuffers()
            OpenGLApi.instance.glFlush()
            if (turnOffVsync) {
                it.setSwapInterval(swapInterval)
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
}

private fun LinuxDrawingSurface.createContext(transparency: Boolean) = createContext(display, transparency)
private fun LinuxDrawingSurface.destroyContext(context: Long) = destroyContext(display, context)
private fun LinuxDrawingSurface.makeCurrent(context: Long) = makeCurrent(display, window, context)
private fun LinuxDrawingSurface.swapBuffers() = swapBuffers(display, window)
private fun LinuxDrawingSurface.setSwapInterval(interval: Int) = setSwapInterval(display, window, interval)

private external fun makeCurrent(display: Long, window: Long, context: Long)
private external fun createContext(display: Long, transparency: Boolean): Long
private external fun destroyContext(display: Long, context: Long)
private external fun setSwapInterval(display: Long, window: Long, interval: Int)
private external fun swapBuffers(display: Long, window: Long)
