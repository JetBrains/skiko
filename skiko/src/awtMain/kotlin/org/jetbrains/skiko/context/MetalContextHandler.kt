package org.jetbrains.skiko.context

import org.jetbrains.skia.*
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
) : JvmContextHandler(layer) {
    override fun initContext(): Boolean {
        try {
            if (context == null) {
                context = makeContext()
                onContextInitialized()
            }
        } catch (e: Exception) {
            Logger.warn(e) { "Failed to create Skia Metal context!" }
            return false
        }
        return true
    }

    override fun initCanvas() {
        disposeCanvas()

        val scale = layer.contentScale
        val width = (layer.backedLayer.width * scale).toInt().coerceAtLeast(0)
        val height = (layer.backedLayer.height * scale).toInt().coerceAtLeast(0)

        if (width > 0 && height > 0) {
            renderTarget = makeRenderTarget(width, height)

            surface = Surface.makeFromBackendRenderTarget(
                context!!,
                renderTarget!!,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.BGRA_8888,
                ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = layer.pixelGeometry)
            ) ?: throw RenderException("Cannot create surface")

            canvas = surface!!.canvas
        } else {
            renderTarget = null
            surface = null
            canvas = null
        }
    }

    override fun flush() {
        super.flush()
        surface?.flushAndSubmit()
        finishFrame()
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

    private fun makeContext() = DirectContext(
        makeMetalContext(device.ptr)
    )

    private fun finishFrame() = finishFrame(device.ptr)

    private external fun makeMetalContext(device: Long): Long
    private external fun makeMetalRenderTarget(device: Long, width: Int, height: Int): Long
    private external fun finishFrame(device: Long)
}
