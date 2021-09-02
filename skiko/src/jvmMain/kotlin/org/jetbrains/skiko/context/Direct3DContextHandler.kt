package org.jetbrains.skiko.context

import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.Native
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.redrawer.Direct3DRedrawer
import java.lang.ref.Reference

internal class Direct3DContextHandler(layer: SkiaLayer) : ContextHandler(layer) {
    private val bufferCount = 2
    private var surfaces: Array<Surface?> = arrayOfNulls(bufferCount)

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
                if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
                    println(rendererInfo())
                }
            }
        } catch (e: Exception) {
            println("${e.message}\nFailed to create Skia Direct3D context!")
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
    private var isD3DInited = false

    override fun initCanvas() {
        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)

        if (isSizeChanged(w, h)) {
            disposeCanvas()
            context?.flush()
            
            if (!isD3DInited) {
                directXRedrawer.initSwapChain(device)
            } else {
                directXRedrawer.resizeBuffers(device, w, h)
            }
            
            try {
                for (bufferIndex in 0..bufferCount - 1) {
                    surfaces[bufferIndex] = directXRedrawer.makeSurface(device, Native.getPtr(context!!), w, h, bufferIndex)
                }
            } finally {
                Reference.reachabilityFence(context!!)
            }

            if (!isD3DInited) {
                isD3DInited = true
                directXRedrawer.initFence(device)
            }
        }
        surface = surfaces[directXRedrawer.getBufferIndex(device)]
        canvas = surface!!.canvas
    }

    override fun flush() {
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
        directXRedrawer.disposeDevice(device)
        context?.close()
    }

    override fun disposeCanvas() {
        for (bufferIndex in 0..bufferCount - 1) {
            surfaces[bufferIndex]?.close()
        }
    }

    override fun rendererInfo(): String {
        return super.rendererInfo() +
            "Video card: ${directXRedrawer.getAdapterName(device)}\n" +
            "Total VRAM: ${directXRedrawer.getAdapterMemorySize(device) / 1024 / 1024} MB\n"
    }
}
