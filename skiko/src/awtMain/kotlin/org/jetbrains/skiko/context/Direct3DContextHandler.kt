package org.jetbrains.skiko.context

import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.Surface
import org.jetbrains.skia.SurfaceProps
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skiko.LayerDrawScope
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.redrawer.Direct3DRedrawer
import java.lang.ref.Reference

internal class Direct3DContextHandler(layer: SkiaLayer) : ContextBasedContextHandler(layer, "Direct3D") {
    private val bufferCount = 2
    private var surfaces: Array<Surface?> = arrayOfNulls(bufferCount)
    private fun isSurfacesNull() = surfaces.all { it == null }

    private val directXRedrawer: Direct3DRedrawer
        get() = layer.redrawer!! as Direct3DRedrawer

    override fun makeContext(): DirectContext = directXRedrawer.makeContext()

    private var currentWidth = 0
    private var currentHeight = 0

    // True while the last initCanvas selected the frame-overlay surface (a live-resize draw), so the next
    // on-screen draw knows to rebuild its surfaces instead of reusing a stale overlay surface/canvas.
    private var lastDrawWasOverlay = false

    override fun LayerDrawScope.initCanvas() {
        val context = context ?: return

        // Direct3D can't work with zero size.
        // Don't rewrite code to skipping, as we need the whole pipeline in zero case too
        // (drawing -> flushing -> swapping -> waiting for vsync)
        val width = scaledLayerWidth.coerceAtLeast(1)
        val height = scaledLayerHeight.coerceAtLeast(1)

        if (directXRedrawer.isInLiveResize && directXRedrawer.liveResizeUsesOverlay) {
            // During a live resize on the overlay path, draw into the frame overlay swapchain (presented
            // synchronously in WM_NCCALCSIZE by presentLiveResizeFrame) instead of the on-screen swapchain. The
            // redrawer owns the overlay surfaces; everything downstream (clear, drawContent, flush) is identical.
            // (The fallback path also sets isInLiveResize but keeps liveResizeUsesOverlay false, so it renders here.)
            lastDrawWasOverlay = true
            try {
                surface = directXRedrawer.liveResizeSurface(getPtr(context), width, height)
                canvas = surface?.canvas
            } finally {
                Reference.reachabilityFence(context)
            }
            return
        }

        val sizeChanged = (width != currentWidth || height != currentHeight)
        if (sizeChanged) {
            currentWidth = width
            currentHeight = height
        }

        if (lastDrawWasOverlay || sizeChanged || isSurfacesNull()) {
            lastDrawWasOverlay = false
            disposeCanvas()
            context.flush()

            val justInitialized = directXRedrawer.changeSize(width, height)
            try {
                val surfaceProps = SurfaceProps(pixelGeometry = pixelGeometry)
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
