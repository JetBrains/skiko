package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.MetalAdapter
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.context.ContextHandler
import java.awt.Graphics2D
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.*

internal class MetalSwingContextHandler(
    private val skiaSwingLayer: SkiaSwingLayer,
    private val adapter: MetalAdapter,
    drawContent: Canvas.() -> Unit
) : SwingContextHandler(drawContent) {
    override val renderApi: GraphicsApi
        get() = skiaSwingLayer.renderApi

    override fun isTransparentBackground(): Boolean = true

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
        val g = graphics

        if (g == null) {
            renderTarget = null
            surface = null
            canvas = null
            return
        }

        val scale = skiaSwingLayer.graphicsConfiguration.defaultTransform.scaleX.toFloat()
        val width = (skiaSwingLayer.width * scale).toInt().coerceAtLeast(0)
        val height = (skiaSwingLayer.height * scale).toInt().coerceAtLeast(0)

        if (width > 0 && height > 0) {
            renderTarget = makeRenderTarget(width, height)

            surface = Surface.makeFromBackendRenderTarget(
                context!!,
                renderTarget!!,
                SurfaceOrigin.TOP_LEFT,
                SurfaceColorFormat.BGRA_8888,
                org.jetbrains.skia.ColorSpace.sRGB,
                SurfaceProps(pixelGeometry = skiaSwingLayer.pixelGeometry)
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
        // TODO: later may be syncCPU is not needed
        surface!!.flushAndSubmit(syncCpu = true)

        val width = surface!!.width
        val height = surface!!.height

        val storage = Bitmap()
        storage.setImageInfo(ImageInfo.makeN32Premul(width, height))
        storage.allocPixels()
        // TODO: it copies pixels from GPU to CPU, so it is really slow
        surface!!.readPixels(storage, 0, 0)

        val bytes = storage.readPixels(storage.imageInfo, (width * 4), 0, 0)
        if (bytes != null) {
            val buffer = DataBufferByte(bytes, bytes.size)
            val raster = Raster.createInterleavedRaster(
                buffer,
                width,
                height,
                width * 4, 4,
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
            val g = graphics
            if (g != null) {
                try {
                    // TODO: a lot of CPU spend for scaling
                    g.drawImage(image, 0, 0, skiaSwingLayer.width, skiaSwingLayer.height, null)
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