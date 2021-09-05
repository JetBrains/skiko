package org.jetbrains.skiko.native.context

import kotlinx.cinterop.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.native.*
import platform.OpenGL.GL_DRAW_FRAMEBUFFER_BINDING
import platform.OpenGL.GL_RGBA8
import platform.OpenGL.glGetIntegerv
import platform.OpenGL.*
import platform.OpenGLCommon.GLenum

internal class OpenGLContextHandler(layer: HardwareLayer) : ContextHandler(layer) {
    override fun initContext(): Boolean {
        println("OpenGLContextHandler::initContext")
        try {
            if (context == null) {
                context = DirectContext.makeGL()
            }
        } catch (e: Exception) {
            println("Failed to create Skia OpenGL context!")
            return false
        }
        return true
    }

    @ExperimentalUnsignedTypes
    private fun openglGetIntegerv(pname: GLenum): UInt {
        var result: UInt = 0U
        memScoped {
            val data = alloc<IntVar>()
            glGetIntegerv(pname, data.ptr);
            result = data.value.toUInt();
        }
        return result
    }

    @ExperimentalUnsignedTypes
    override fun initCanvas() {
        println("OpenGLContextHandler::initCanvas")
        dispose()

        val scale = layer.contentScale
        val w = (layer.nsView.frame.useContents { size.width } * scale).toInt().coerceAtLeast(0)
        val h = (layer.nsView.frame.useContents { size.height } * scale).toInt().coerceAtLeast(0)

            val fbId = openglGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING.toUInt())
            renderTarget = BackendRenderTarget.makeGL(
                w,
                h,
                0,
                8,
                fbId.toInt(),
                FramebufferFormat.GR_GL_RGBA8
            )
            surface = Surface.makeFromBackendRenderTarget(
                context!!,
                renderTarget!!,
                SurfaceOrigin.BOTTOM_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.sRGB
            )

        canvas = surface!!.canvas
            ?: error("Could not obtain Canvas from Surface")
    }
}
