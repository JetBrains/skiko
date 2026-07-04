package org.jetbrains.skiko.context

import kotlinx.cinterop.*
import org.jetbrains.skia.*
import org.jetbrains.skiko.LayerDrawScope
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkiaLayer

internal class LinuxOpenGLContextHandler(layer: SkiaLayer) : ContextHandler(layer, layer::draw) {
    override fun initContext(): Boolean {
        try {
            if (context == null) {
                context = DirectContext.makeGL()
            }
        } catch (_: Exception) {
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

    override fun LayerDrawScope.initCanvas() {
        val w = scaledLayerWidth
        val h = scaledLayerHeight
        if (isSizeChanged(w, h)) {
            disposeCanvas()
            renderTarget = BackendRenderTarget.makeGL(
                w,
                h,
                0,
                8,
                0, // fbId = 0 (default framebuffer)
                FramebufferFormat.GR_GL_RGBA8
            )
            surface = Surface.makeFromBackendRenderTarget(
                context!!,
                renderTarget!!,
                SurfaceOrigin.BOTTOM_LEFT,
                SurfaceColorFormat.RGBA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = layer.pixelGeometry)
            ) ?: throw RenderException("Cannot create surface")

            canvas = surface?.canvas
                ?: error("Could not obtain Canvas from Surface")
        }
    }
}
