package org.jetbrains.skiko.context

import org.jetbrains.skija.ColorSpace
import org.jetbrains.skija.Surface
import org.jetbrains.skija.SurfaceColorFormat
import org.jetbrains.skija.SurfaceOrigin
import org.jetbrains.skija.impl.Native
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.hostFullName
import org.jetbrains.skiko.javaLocation
import org.jetbrains.skiko.javaVendor
import org.jetbrains.skiko.redrawer.MetalRedrawer

internal class MetalContextHandler(layer: SkiaLayer) : ContextHandler(layer) {
    val metalRedrawer: MetalRedrawer
        get() = layer.redrawer!! as MetalRedrawer

    override fun initContext(): Boolean {
        try {
            if (context == null) {
                context = metalRedrawer.makeContext()
                if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
                    println(hardwareInfo())
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
            ColorSpace.getSRGB()
        )

        canvas = surface!!.canvas
    }

    override fun flush() {
        super.flush()
        surface!!.flushAndSubmit()
        metalRedrawer.finishFrame()
    }

    override fun destroyContext() {
        context?.close()
    }

    override fun hardwareInfo(): String {
        return "METAL rendering info:\n" +
            "OS: $hostFullName\n" +
            "Java: $javaVendor\n" +
            "Java location: $javaLocation\n" +
            "Video card: ${metalRedrawer.getAdapterName()}\n" +
            "Total VRAM: ${metalRedrawer.getAdapterMemorySize() / 1024 / 1024} MB\n"
    }
}
