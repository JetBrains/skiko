package org.jetbrains.skiko.context

import org.jetbrains.skia.*
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.redrawer.MetalRedrawer

internal class MetalContextHandler(layer: SkiaLayer) : JvmContextHandler(layer) {
    val metalRedrawer: MetalRedrawer
        get() = layer.redrawer!! as MetalRedrawer

    override fun initContext(): Boolean {
        try {
            if (context == null) {
                context = metalRedrawer.makeContext()
                if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
                    Logger.info("Renderer info:\n ${rendererInfo()}")
                }
            }
        } catch (e: Exception) {
            Logger.warn("${e.message}\nFailed to create Skia Metal context!")
            return false
        }
        return true
    }

    override fun initCanvas() {
        disposeCanvas()

        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)

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
        super.flush()
        surface?.flushAndSubmit()
        metalRedrawer.finishFrame()
    }

    override fun rendererInfo(): String {
        return super.rendererInfo() +
            "Video card: ${metalRedrawer.adapterName}\n" +
            "Total VRAM: ${metalRedrawer.adapterMemorySize / 1024 / 1024} MB\n"
    }
}
