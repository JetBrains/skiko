package org.jetbrains.skiko.context

import org.jetbrains.skia.BackendRenderTarget
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.FramebufferFormat
import org.jetbrains.skia.PixelGeometry
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkiaLayer

internal class LinuxOpenGLContextHandler(
    layer: SkiaLayer,
) : ContextHandler(layer, layer::draw) {
    private var currentWidth = 0
    private var currentHeight = 0

    override fun initContext(): Boolean {
        return try {
            if (context == null) {
                context = DirectContext.makeGL()
            }
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun isSizeChanged(width: Int, height: Int): Boolean {
        if (width != currentWidth || height != currentHeight) {
            currentWidth = width
            currentHeight = height
            return true
        }
        return false
    }

    override fun initCanvas() {
        val x11Window = layer.x11Window ?: return
        val scale = layer.contentScale
        val w = (x11Window.width * scale).toInt().coerceAtLeast(0)
        val h = (x11Window.height * scale).toInt().coerceAtLeast(0)

        if (w <= 0 || h <= 0) {
            disposeCanvas()
            canvas = null
            return
        }

        if (isSizeChanged(w, h) || surface == null) {
            disposeCanvas()

            // Render into the default framebuffer.
            renderTarget = BackendRenderTarget.makeGL(
                w,
                h,
                0,
                8,
                0,
                FramebufferFormat.GR_GL_RGBA8,
            )

            surface = Surface.makeFromBackendRenderTarget(
                context!!,
                renderTarget!!,
                SurfaceOrigin.BOTTOM_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = PixelGeometry.UNKNOWN),
            ) ?: throw RenderException("Cannot create OpenGL surface")
        }

        canvas = surface!!.canvas
    }
}

