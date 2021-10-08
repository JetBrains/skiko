package org.jetbrains.skiko.context

import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceColorFormat
import org.jetbrains.skia.SurfaceOrigin
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.redrawer.MetalRedrawer
import javax.swing.JFrame
import javax.swing.SwingUtilities
import java.awt.Dimension

internal class MetalContextHandler(layer: SkiaLayer) : ContextHandler(layer) {
    val metalRedrawer: MetalRedrawer
        get() = layer.redrawer!! as MetalRedrawer

    override fun initContext(): Boolean {
        try {
            if (context == null) {
                context = metalRedrawer.makeContext()
                if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
                    println(rendererInfo())
                }
            }
        } catch (e: Exception) {
            println("${e.message}\nFailed to create Skia Metal context!")
            return false
        }
        return true
    }

    override fun initCanvas() {
        disposeCanvas()

        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)

        renderTarget = metalRedrawer.makeRenderTarget(w, h)

        surface = Surface.makeFromBackendRenderTarget(
            context!!,
            renderTarget!!,
            SurfaceOrigin.TOP_LEFT,
            SurfaceColorFormat.BGRA_8888,
            ColorSpace.sRGB
        )

        canvas = surface!!.canvas
    }

    private var fullscreenEvent = false
    private val originalBounds = Dimension(0, 0)

    override fun flush() {
        super.flush()
        surface!!.flushAndSubmit()
        metalRedrawer.finishFrame()

        val window = SwingUtilities.getRoot(layer) as JFrame
        if (fullscreenEvent && !layer.fullscreen && layer.transparency) {
            window.setSize(originalBounds.width, originalBounds.height)
            fullscreenEvent = false
        }
        if (!fullscreenEvent && !layer.fullscreen && layer.transparency) {
            originalBounds.width = window.width
            originalBounds.height = window.height
        }
        if (!fullscreenEvent && layer.fullscreen && layer.transparency) {
            fullscreenEvent = true
            val display = window.graphicsConfiguration.device.displayMode
            window.setSize(display.getWidth(), display.getHeight())
        }
    }

    override fun rendererInfo(): String {
        return super.rendererInfo() +
            "Video card: ${metalRedrawer.getAdapterName()}\n" +
            "Total VRAM: ${metalRedrawer.getAdapterMemorySize() / 1024 / 1024} MB\n"
    }
}
