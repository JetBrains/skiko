package org.jetbrains.skiko.context

import org.jetbrains.skija.ColorSpace
import org.jetbrains.skija.FramebufferFormat
import org.jetbrains.skija.Surface
import org.jetbrains.skija.SurfaceColorFormat
import org.jetbrains.skija.SurfaceOrigin
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
                    println(hardwareInfo())
                }
            }
        } catch (e: Exception) {
            println("Failed to create Skia OpenGL context!")
            return false
        }
        return true
    }

    override fun initCanvas() {
        disposeCanvas()

        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)

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
            ColorSpace.getSRGB()
        )

        canvas = surface!!.canvas
    }

    override fun hardwareInfo(): String {
        val gl = OpenGLApi.instance
        return  "OPENGL hardware info:\n" +
            "Vendor: ${gl.glGetString(gl.GL_VENDOR)}\n" +
            "Model: ${gl.glGetString(gl.GL_RENDERER)}\n" +
            "Total memory: ${gl.glGetIntegerv(gl.GL_TOTAL_MEMORY) / 1024} MB\n"
    }
}
