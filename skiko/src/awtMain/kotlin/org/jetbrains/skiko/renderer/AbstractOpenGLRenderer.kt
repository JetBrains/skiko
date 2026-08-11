package org.jetbrains.skiko.renderer

import org.jetbrains.skia.*
import org.jetbrains.skiko.*

/**
 * Common base of the platform-specific OpenGL renderers.
 */
internal abstract class AbstractOpenGLRenderer(
    layer: SkiaLayer,
    analytics: SkiaLayerAnalytics
) : ContextBasedRenderer(layer, analytics, GraphicsApi.OPENGL, "OpenGL") {

    override fun makeContext() = makeGLContext()

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

    override fun LayerDrawScope.initCanvas() {
        val w = scaledLayerWidth
        val h = scaledLayerHeight

        if (isSizeChanged(w, h) || surface == null) {
            disposeCanvas()
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
                context!!,
                renderTarget!!,
                SurfaceOrigin.BOTTOM_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = pixelGeometry)
            ) ?: throw RenderException("Cannot create surface")
        }

        canvas = surface!!.canvas
    }

    override val renderInfo: String
        get() {
            val gl = OpenGLApi.instance
            return super.renderInfo +
                    "Vendor: ${gl.glGetString(gl.GL_VENDOR)}\n" +
                    "Model: ${gl.glGetString(gl.GL_RENDERER)}\n" +
                    "Total VRAM: ${gl.glGetIntegerv(gl.GL_TOTAL_MEMORY) / 1024} MB\n"
        }
}
