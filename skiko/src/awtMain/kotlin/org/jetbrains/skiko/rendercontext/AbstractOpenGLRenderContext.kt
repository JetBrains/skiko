package org.jetbrains.skiko.rendercontext

import org.jetbrains.skia.*
import org.jetbrains.skiko.*

internal abstract class AbstractOpenGLRenderContext(
    host: AwtSurfaceHost,
    analytics: SkiaLayerAnalytics,
    private val properties: SkiaLayerProperties,
) : AwtRenderContext(host, analytics, GraphicsApi.OPENGL) {

    override val directContext: DirectContext? get() = glContext

    protected val adapterName get() = OpenGLApi.instance.glGetString(OpenGLApi.instance.GL_RENDERER)

    private var glContext: DirectContext? = null
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var canvas: Canvas? = null
    private var currentWidth = 0
    private var currentHeight = 0

    override val renderInfo: String
        get() {
            val gl = OpenGLApi.instance
            return renderInfoHeader(host.renderApi) +
                    "Vendor: ${gl.glGetString(gl.GL_VENDOR)}\n" +
                    "Model: ${gl.glGetString(gl.GL_RENDERER)}\n" +
                    "Total VRAM: ${gl.glGetIntegerv(gl.GL_TOTAL_MEMORY) / 1024} MB\n"
        }

    protected fun LayerDrawScope.drawFrame() {
        if (!ensureContext()) {
            throw RenderException("Cannot init graphic context")
        }
        initSurface()
        canvas?.runRestoringState {
            clear(Color.TRANSPARENT)
            host.draw(this)
        }
        glContext?.flush()
    }

    /** The surface the last [createSurface] produced, or `null` before the first frame. */
    protected val glSurface: Surface? get() = surface

    /** Submits the recorded GPU work. The caller must have made its context current. */
    protected fun flushGl() {
        glContext?.flush()
    }

    protected fun disposeGlResources() {
        disposeSurface()
        glContext?.close()
        glContext = null
    }

    protected fun ensureContext(): Boolean {
        if (glContext == null) {
            try {
                val newContext = makeGLContext()
                glContext = newContext
                onContextInitialized(newContext, properties.gpuResourceCacheLimit) { renderInfo }
            } catch (e: Exception) {
                Logger.warn(e) { "Failed to create Skia OpenGL context!" }
                return false
            }
        }
        return true
    }

    private fun LayerDrawScope.initSurface() = createSurface(scaledLayerWidth, scaledLayerHeight, pixelGeometry)

    /** (Re)creates the on-screen GL surface at [w] x [h] when the size changed or none exists yet. */
    protected fun createSurface(w: Int, h: Int, pixelGeometry: PixelGeometry) {
        val glContext = glContext ?: return

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
