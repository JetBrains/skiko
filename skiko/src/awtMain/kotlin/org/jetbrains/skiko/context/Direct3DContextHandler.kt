package org.jetbrains.skiko.context

import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.redrawer.Direct3DRedrawer
import java.lang.ref.Reference

internal class Direct3DContextHandler(layer: SkiaLayer) : JvmContextHandler(layer) {
    private val bufferCount = 2
    private var surfaces: Array<Surface?> = arrayOfNulls(bufferCount)
    private fun isSurfacesNull() = surfaces.all { it == null }

    private val directXRedrawer: Direct3DRedrawer
        get() = layer.redrawer!! as Direct3DRedrawer

    override fun initContext(): Boolean {
        try {
            if (context == null) {
                context = directXRedrawer.makeContext()
                onContextInitialized()
            }
        } catch (e: Exception) {
            Logger.warn(e) { "Failed to create Skia Direct3D context!" }
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
        val context = context ?: return
        val scale = layer.contentScale

        // Direct3D can't work with zero size.
        // Don't rewrite code to skipping, as we need the whole pipeline in zero case too
        // (drawing -> flushing -> swapping -> waiting for vsync)
        val width = (layer.width * scale).toInt().coerceAtLeast(1)
        val height = (layer.height * scale).toInt().coerceAtLeast(1)

        if (isSizeChanged(width, height) || isSurfacesNull()) {
            disposeCanvas()
            context.flush()

            val justInitialized = directXRedrawer.changeSize(width, height)
            try {
                val surfaceProps = SurfaceProps(pixelGeometry = layer.pixelGeometry)
                for (bufferIndex in 0 until bufferCount) {
                    surfaces[bufferIndex] = directXRedrawer.makeSurface(
                        context = getPtr(context),
                        width = width,
                        height = height,
                        surfaceProps = surfaceProps,
                        index = bufferIndex
                    )
                }
            } finally {
                Reference.reachabilityFence(context)
            }

            if (justInitialized) {
                directXRedrawer.initFence()
            }
        }
        surface = surfaces[directXRedrawer.getBufferIndex()]
        canvas = surface!!.canvas
    }

    override fun flush() {
        val context = context ?: return
        val surface = surface ?: return
        try {
            flush(getPtr(context), getPtr(surface))
        } finally {
            Reference.reachabilityFence(context)
            Reference.reachabilityFence(surface)
        }
    }

    override fun disposeCanvas() {
        for (bufferIndex in 0 until bufferCount) {
            surfaces[bufferIndex]?.close()
        }
    }

    override fun rendererInfo(): String {
        return super.rendererInfo() +
            "Video card: ${directXRedrawer.adapterName}\n" +
            "Total VRAM: ${directXRedrawer.adapterMemorySize / 1024 / 1024} MB\n"
    }

    private external fun flush(context: Long, surface: Long)
}
