package org.jetbrains.skiko.context

import java.lang.ref.Reference
import org.jetbrains.skija.ColorSpace
import org.jetbrains.skija.FramebufferFormat
import org.jetbrains.skija.Picture
import org.jetbrains.skija.Surface
import org.jetbrains.skija.SurfaceColorFormat
import org.jetbrains.skija.SurfaceOrigin
import org.jetbrains.skija.impl.Native
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.redrawer.AngleRedrawer
import org.jetbrains.skiko.redrawer.Redrawer
import org.jetbrains.skiko.destroyContext

internal class AngleContextHandler(layer: SkiaLayer) : ContextHandler(layer) {
    val angleRedrawer: AngleRedrawer
        get() = layer.redrawer!! as AngleRedrawer

    var device: Long = 0
    override fun initContext(): Boolean {
        try {
            if (context == null) {
                device = angleRedrawer.createDevice()
                if (device == 0L) {
                    throw Exception("Failed to create Angle device.")
                }
                context = angleRedrawer.makeContext(device)
            }
        } catch (e: Exception) {
            println("${e.message}\nFailed to create Skia Angle context!")
            return false
        }
        return true
    }

    override fun initCanvas() {
        dispose()

        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)

        renderTarget = angleRedrawer.makeRenderTarget(device, w, h)

        surface = Surface.makeFromBackendRenderTarget(
            context!!,
            renderTarget!!,
            SurfaceOrigin.TOP_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.getSRGB()
        )

        canvas = surface!!.canvas
    }

    override fun flush() {
        super.flush()
        try {
            angleRedrawer.finishFrame(
                device,
                Native.getPtr(context!!),
                Native.getPtr(surface!!)
            )
        } finally {
            Reference.reachabilityFence(context!!)
            Reference.reachabilityFence(surface!!)
        }
    }

    override fun destroyContext() {
        destroyContext(Native.getPtr(context!!))
    }
}
