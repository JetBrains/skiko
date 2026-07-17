package org.jetbrains.skiko.context

import org.jetbrains.skia.*
import org.jetbrains.skiko.LayerDrawScope
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.MetalAdapter
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.redrawer.MetalDevice

/**
 * Provides a way to draw on Skia canvas created in [layer] bounds using Metal GPU acceleration.
 *
 * For each [ContextHandler.draw] request it initializes Skia Canvas with Metal context and
 * draws [SkiaLayer.draw] content in this canvas.
 *
 * @see "src/awtMain/objectiveC/macos/MetalContextHandler.mm" -- native implementation
 */
internal class MetalContextHandler(
    layer: SkiaLayer,
    private val device: MetalDevice,
    private val adapter: MetalAdapter
) : ContextBasedContextHandler(layer, "Metal") {
    override fun LayerDrawScope.initCanvas() {
        disposeCanvas()

        val width = scaledLayerWidth
        val height = scaledLayerHeight

        if (width > 0 && height > 0) {
            renderTarget = makeRenderTarget(width, height)

            surface = Surface.makeFromBackendRenderTarget(
                context!!,
                renderTarget!!,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.BGRA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = pixelGeometry)
            ) ?: throw RenderException("Cannot create surface")

            canvas = surface!!.canvas
        } else {
            renderTarget = null
            surface = null
            canvas = null
        }
    }

    // After `flush` you also need to call `finishFrame` (or `finishFrameInLiveResize`).
    public override fun flush() {
        super.flush()
        surface?.flushAndSubmit()
        Logger.debug { "MetalContextHandler finished drawing frame" }
    }

    override fun rendererInfo(): String {
        return super.rendererInfo() +
                "Video card: ${adapter.name}\n" +
                "Total VRAM: ${adapter.memorySize / 1024 / 1024} MB\n"
    }

    private fun makeRenderTarget(width: Int, height: Int) = BackendRenderTarget(
        makeMetalRenderTarget(device.ptr, width, height)
    )

    override fun makeContext() = DirectContext(
        makeMetalContext(device.ptr)
    )

    /** Presents the frame asynchronously (off the main thread). Used for every frame outside a live resize. */
    fun finishFrame() = finishFrame(device.ptr)

    /**
     * Presents the frame synchronously, joining the ambient window-resize transaction.
     * Must be called on the AppKit main thread during a live resize.
     */
    fun finishFrameInLiveResize() = finishFrameInLiveResize(device.ptr)

    private external fun makeMetalContext(device: Long): Long
    private external fun makeMetalRenderTarget(device: Long, width: Int, height: Int): Long
    private external fun finishFrame(device: Long)
    private external fun finishFrameInLiveResize(device: Long)
}
