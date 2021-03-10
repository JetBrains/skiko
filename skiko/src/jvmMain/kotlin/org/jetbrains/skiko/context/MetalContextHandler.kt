package org.jetbrains.skiko.context

import java.lang.ref.Reference
import org.jetbrains.skija.ColorSpace
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.Surface
import org.jetbrains.skija.SurfaceColorFormat
import org.jetbrains.skija.SurfaceOrigin
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.redrawer.MetalRedrawer
import javax.swing.SwingUtilities.convertPoint
import javax.swing.SwingUtilities.getRootPane

internal class MetalContextHandler(layer: SkiaLayer) : ContextHandler(layer) {
    val metalRedrawer: MetalRedrawer
        get() = layer.redrawer!! as MetalRedrawer
    var device: Long = 0

    override fun initContext(): Boolean {
        try {
            if (context == null) {
                device = metalRedrawer.createDevice()
                if (device == 0L) {
                    throw Exception("Failed to create Metal device.")
                }
                context = metalRedrawer.makeContext(device)
            }
        } catch (e: Exception) {
            println("${e.message}\nFailed to create Skia Metal context!")
            return false
        }
        return true
    }

    override fun initCanvas() {
        dispose()

        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)

        renderTarget = metalRedrawer.makeRenderTarget(device, w, h)

        surface = Surface.makeFromBackendRenderTarget(
            context!!,
            renderTarget!!,
            SurfaceOrigin.TOP_LEFT,
            SurfaceColorFormat.BGRA_8888,
            ColorSpace.getSRGB()
        )

        canvas = surface!!.canvas
    }

    override fun flush() {
        super.flush()
        surface!!.flushAndSubmit()
        metalRedrawer.finishFrame(device)
    }
}
