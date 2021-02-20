package org.jetbrains.skiko.native.context

import kotlinx.cinterop.*
import org.jetbrains.skiko.skia.native.*
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
                context = GrDirectContext.MakeGL()
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

        // TODO: Skia C++ interop: glInfo is a `struct`, not `class`,
        // so we fallback to C interop here.
        memScoped {
            val glInfo: GrGLFramebufferInfo = alloc<GrGLFramebufferInfo>()
            glInfo.fFBOID = fbId
            glInfo.fFormat = GL_RGBA8.toUInt()
            renderTarget = GrBackendRenderTarget(w, h, 0, 8, glInfo.readValue())
        }

        surface = SkSurface.MakeFromBackendRenderTarget(
            // TODO: C++ interop knows nothing about inheritance.
            context!!.ptr.reinterpret<GrRecordingContext>(),
            renderTarget!!.ptr,
            GrSurfaceOrigin.kBottomLeft_GrSurfaceOrigin,
            colorType = kRGBA_8888_SkColorType,
            colorSpace = SkColorSpace.MakeSRGB(),
            surfaceProps = null,
            releaseProc = null,
            releaseContext = null
        ) ?: error("Could not obtain SkSurface")

        canvas = surface!!.getCanvas()?.pointed
            ?: error("Could not obtain SkCanvas from SkSurface")
    }
}
