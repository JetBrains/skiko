package org.jetbrains.skiko.context

import org.jetbrains.skia.*
import org.jetbrains.skiko.*
import java.awt.Color
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.*

internal class MetalOffScreenContextHandler(
    layer: SkiaLayer,
    private val adapter: MetalAdapter
) : JvmContextHandler(layer) {
    override fun initContext(): Boolean {
        try {
            if (context == null) {
                context = makeMetalContext()
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
                org.jetbrains.skia.ColorSpace.sRGB,
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
        surface!!.flushAndSubmit(syncCpu = true)

        val w = surface!!.width
        val h = surface!!.height

        val storage = Bitmap()
        storage.setImageInfo(ImageInfo.makeN32Premul(w, h))
        storage.allocPixels()
        // TODO: it copies pixels from GPU to CPU, so it is really slow
        surface!!.readPixels(storage, 0, 0)

        val bytes = storage.readPixels(storage.imageInfo, (w * 4), 0, 0)
        if (bytes != null) {
            val buffer = DataBufferByte(bytes, bytes.size)
            val raster = Raster.createInterleavedRaster(
                buffer,
                w,
                h,
                w * 4, 4,
                intArrayOf(2, 1, 0, 3), // BGRA order
                null
            )
            val colorModel = ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_sRGB),
                true,
                false,
                Transparency.TRANSLUCENT,
                DataBuffer.TYPE_BYTE
            )
            val image = BufferedImage(colorModel, raster!!, false, null)
            val g = layer.backedLayer.graphics
            if (g != null) {
                try {
                    if (!layer.fullscreen && layer.transparency && hostOs == OS.MacOS) {
                        g.color = Color(0, 0, 0, 0)
                        g.clearRect(0, 0, w, h)
                    }
                    // TODO: a lot of CPU spend for scaling
                    g.drawImage(image, 0, 0, layer.width, layer.height, null)
                } finally {
                    g.dispose()
                }
            }
        }
    }

    override fun rendererInfo(): String {
        return super.rendererInfo() +
                "Video card: ${adapter.name}\n" +
                "Total VRAM: ${adapter.memorySize / 1024 / 1024} MB\n"
    }

    private fun makeRenderTarget(width: Int, height: Int) = BackendRenderTarget(
        makeMetalRenderTargetOffScreen(adapter.ptr, width, height)
    )

    private fun makeMetalContext(): DirectContext = DirectContext(
        makeMetalContext(adapter.ptr)
    )

    private external fun makeMetalContext(adapter: Long): Long

    private external fun makeMetalRenderTargetOffScreen(adapter: Long, width: Int, height: Int): Long
}