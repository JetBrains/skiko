package org.jetbrains.skiko.context

import org.jetbrains.skija.ColorSpace
import org.jetbrains.skija.Surface
import org.jetbrains.skija.SurfaceColorFormat
import org.jetbrains.skija.SurfaceOrigin
import org.jetbrains.skija.impl.Native
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.redrawer.Direct3DRedrawer
import java.lang.ref.Reference

internal class Direct3DContextHandler(layer: SkiaLayer) : ContextHandler(layer) {
    val directXRedrawer: Direct3DRedrawer
        get() = layer.redrawer!! as Direct3DRedrawer

    var device: Long = 0
    override fun initContext(): Boolean {
        try {
            if (context == null) {
                device = directXRedrawer.createDevice()
                if (device == 0L) {
                    throw Exception("Failed to create DirectX12 device.")
                }
                context = directXRedrawer.makeContext(device)
                val printDeviceEnabled = System.getProperty("skiko.hardwareInfo.enabled") == "true"
                if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
                    println(hardwareInfo())
                }
            }
        } catch (e: Exception) {
            println("${e.message}\nFailed to create Skia Direct3D context!")
            return false
        }
        return true
    }

    override fun initCanvas() {
        disposeCanvas()

        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)

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
        super.flush()
        try {
            directXRedrawer.finishFrame(
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
        context?.close()
        directXRedrawer.disposeDevice(device)
    }

    override fun hardwareInfo(): String {
        return "DIRECT3D (dx12) hardware info:\n" +
            "Video card: ${directXRedrawer.getAdapterName(device)}\n" +
            "Total memory: ${directXRedrawer.getAdapterMemorySize(device) / 1024 / 1024} MB\n"
    }
}
