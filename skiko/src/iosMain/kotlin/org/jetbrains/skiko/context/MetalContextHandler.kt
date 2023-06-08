package org.jetbrains.skiko.context

import kotlinx.cinterop.useContents
import org.jetbrains.skia.*
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.redrawer.MetalRedrawer

internal class MetalContextHandler(layer: SkiaLayer) : ContextHandler(layer, layer::draw) {
    val metalRedrawer: MetalRedrawer
        get() = layer.redrawer!!

    override fun initContext(): Boolean {
        try {
            if (context == null) {
                context = metalRedrawer.makeContext()
            }
        } catch (e: Exception) {
            println("${e.message}\nFailed to create Skia Metal context!")
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
        disposeCanvas()
        val scale = layer.contentScale
        val (w, h) = layer.view!!.frame.useContents {
            (size.width * scale).toInt().coerceAtLeast(0) to (size.height * scale).toInt().coerceAtLeast(0)
        }

        if (isSizeChanged(w, h)) {
            metalRedrawer.syncSize()
        }

        if (w > 0 && h > 0) {
            renderTarget = metalRedrawer.makeRenderTarget(w, h)

            surface = Surface.makeFromBackendRenderTarget(
                context!!,
                renderTarget!!,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.BGRA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = layer.pixelGeometry)
            ) ?: throw RenderException("Cannot create surface")

            canvas = surface!!.canvas
        } else {
            renderTarget = null
            surface = null
            canvas = null
        }
    }

    override fun flush() {
        // TODO: maybe make flush async as in JVM version.
        super.flush()
        surface?.flushAndSubmit()
        metalRedrawer.finishFrame()
    }

   override fun rendererInfo(): String {
        return "Native Metal: device ${metalRedrawer.device.name}"
    }
}

