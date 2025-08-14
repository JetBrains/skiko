package org.jetbrains.skiko.context

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.FramebufferFormat
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.SkiaLayer
import platform.android.ANativeWindow_getHeight
import platform.android.ANativeWindow_getWidth
import platform.gles31.GL_DRAW_FRAMEBUFFER_BINDING
import platform.gles31.GL_RENDERER
import platform.gles31.GL_VENDOR
import platform.gles31.glGetIntegerv
import platform.gles31.glGetString

internal class AndroidNativeOpenGLContextHandler(layer: SkiaLayer) :
    ContextHandler(layer, layer::draw) {

    override fun initContext(): Boolean {
        try {
            if (context == null) {
                context = DirectContext.makeGL()
            }
        } catch (e: Exception) {
            Logger.warn(e) { "Failed to create Skia OpenGL context!" }
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
        val window = layer.nativeWindow ?: return
        val width = ANativeWindow_getWidth(window)
        val height = ANativeWindow_getHeight(window)

        if (isSizeChanged(width, height) || surface == null) {
            disposeCanvas()
            val fbId = memScoped {
                val outFbId = alloc<IntVar>()
                glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, outFbId.ptr)
                outFbId.value
            }
            renderTarget = BackendRenderTarget.makeGL(
                width,
                height,
                0,  // samples
                8,  // stencilBits
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

        canvas = surface?.canvas
    }

    override fun rendererInfo(): String {
        return super.rendererInfo() +
                "Vendor: ${glGetString(GL_VENDOR)?.reinterpret<ByteVar>()?.toKString()}\n" +
                "Model: ${glGetString(GL_RENDERER)?.reinterpret<ByteVar>()?.toKString()}\n"
    }
}
