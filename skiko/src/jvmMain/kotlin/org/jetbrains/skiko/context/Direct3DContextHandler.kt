package org.jetbrains.skiko.context

import org.jetbrains.skija.ColorSpace
import org.jetbrains.skija.FramebufferFormat
import org.jetbrains.skija.Picture
import org.jetbrains.skija.Surface
import org.jetbrains.skija.SurfaceColorFormat
import org.jetbrains.skija.SurfaceOrigin
import org.jetbrains.skija.impl.Native
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.redrawer.Direct3DRedrawer
import org.jetbrains.skiko.redrawer.Redrawer

internal class Direct3DContextHandler(layer: SkiaLayer) : ContextHandler(layer) {
    val directXRedrawer: Direct3DRedrawer
        get() = layer.redrawer!! as Direct3DRedrawer

    var device: Long = 0
    override fun initContext(): Boolean {
        try {
            if (context == null) {
                device = directXRedrawer.createDevice()
                directXRedrawer.createSwapChain(layer.windowHandle, device)
                context = directXRedrawer.makeContext(device)
            }
        } catch (e: Exception) {
            println("Failed to create Skia Direct3D context!\n ${e.stackTrace}")
            return false
        }
        return true
    }

    

    override fun initCanvas() {
        dispose()

        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)

        directXRedrawer.resizeBuffers(device, w, h)

        renderTarget = directXRedrawer.makeRenderTarget(device, w, h)

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
        directXRedrawer.finishFrame(
            device,
            Native.getPtr(context!!),
            Native.getPtr(surface!!)
        );
    }
}
