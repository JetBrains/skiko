package org.jetbrains.skiko.context

import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.FramebufferFormat
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skiko.OpenGLApi
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.makeGLContext
import org.jetbrains.skiko.makeGLRenderTarget

internal class OpenGLContextHandler(layer: SkiaLayer) : ContextHandler(layer) {
    override fun initContext(): Boolean {
        try {
            if (context == null) {
                context = makeGLContext()
                if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
                    println(rendererInfo())
                }
            }
        } catch (e: Exception) {
            println("Failed to create Skia OpenGL context!")
            return false
        }
        return true
    }

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

    override fun initCanvas() {
        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)

        if (isSizeChanged(w, h)) {
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
                ColorSpace.sRGB
            )
        }

        canvas = surface!!.canvas
    }

    override fun rendererInfo(): String {
        val gl = OpenGLApi.instance
       return super.rendererInfo() +
            "Vendor: ${gl.glGetString(gl.GL_VENDOR)}\n" +
            "Model: ${gl.glGetString(gl.GL_RENDERER)}\n" +
            "Total VRAM: ${gl.glGetIntegerv(gl.GL_TOTAL_MEMORY) / 1024} MB\n"
    }
}
