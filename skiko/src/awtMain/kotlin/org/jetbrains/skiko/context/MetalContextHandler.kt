package org.jetbrains.skiko.context

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.RenderException
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
    private val layer: SkiaLayer,
    private val device: MetalDevice,
    private val adapterInfo: AdapterInfo? = null
) {
    private var context: DirectContext? = null

    private fun initContext(): Boolean {
        try {
            if (context == null) {
                context = DirectContext(
                    makeMetalContext(device.ptr)
                )
                if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
                    Logger.info { "Renderer info:\n ${rendererInfo()}" }
                }
            }
        } catch (e: Exception) {
            Logger.warn(e) { "Failed to create Skia Metal context!" }
            return false
        }
        return true
    }

    fun dispose() {
        context?.close()
    }

    fun rendererInfo(): String {
        return "GraphicsApi: ${layer.renderApi}\n" +
                "OS: ${hostOs.id} ${hostArch.id}\n" +
                if (adapterInfo != null) {
                    "Video card: ${adapterInfo.adapterName}\n" + "Total VRAM: ${adapterInfo.adapterMemorySize / 1024 / 1024} MB\n"
                } else {
                    ""
                }
    }

    // throws RenderException if initialization of graphic context was not successful
    fun draw() {
        if (!initContext()) {
            throw RenderException("Cannot init graphic context")
        }
        val scale = layer.contentScale
        val width = (layer.backedLayer.width * scale).toInt().coerceAtLeast(0)
        val height = (layer.backedLayer.height * scale).toInt().coerceAtLeast(0)

        if (width > 0 && height > 0) {
            BackendRenderTarget(makeMetalRenderTarget(device.ptr, width, height)).use { renderTarget ->
                val surface = Surface.makeFromBackendRenderTarget(
                    context!!,
                    renderTarget,
                    SurfaceOrigin.TOP_LEFT,
                    SurfaceColorFormat.BGRA_8888,
                    ColorSpace.sRGB,
                    SurfaceProps(pixelGeometry = layer.pixelGeometry)
                ) ?: throw RenderException("Cannot create surface")

                surface.use {
                    val canvas = surface.canvas
                    canvas.clear(0)
                    layer.draw(canvas)

                    context?.flush()
                    surface.flushAndSubmit()
                    finishFrame(device.ptr)
                }
            }
        } else {
            context?.flush()
            finishFrame(device.ptr)
        }
    }

    private external fun makeMetalContext(device: Long): Long
    private external fun makeMetalRenderTarget(device: Long, width: Int, height: Int): Long
    private external fun finishFrame(device: Long)

    data class AdapterInfo(val adapterName: String, val adapterMemorySize: Long)
}
