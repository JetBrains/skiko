package org.jetbrains.skiko.swing

import org.jetbrains.skia.*
import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.Logger
import org.jetbrains.skiko.context.ContextHandler
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.*

internal class SoftwareSwingContextHandler(
    private val skiaSwingLayer: SkiaSwingLayer,
    drawContent: Canvas.() -> Unit
) : SwingContextHandler(drawContent) {
    override val renderApi: GraphicsApi
        get() = skiaSwingLayer.renderApi

    override fun isTransparentBackground(): Boolean = true

    private val colorModel = ComponentColorModel(
        ColorSpace.getInstance(ColorSpace.CS_sRGB),
        true,
        false,
        Transparency.TRANSLUCENT,
        DataBuffer.TYPE_BYTE
    )
    private val storage = Bitmap()
    private var image: BufferedImage? = null
    private var raster: WritableRaster? = null
    private var isInited = false

    override fun initContext(): Boolean {
        // Raster does not need context
        if (!isInited) {
            if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
                Logger.info { "Renderer info:\n ${rendererInfo()}" }
            }
            isInited = true
        }
        return isInited
    }

    override fun initCanvas() {
        disposeCanvas()

        val scale = skiaSwingLayer.graphicsConfiguration.defaultTransform.scaleX.toFloat()
        val w = (skiaSwingLayer.width * scale).toInt().coerceAtLeast(0)
        val h = (skiaSwingLayer.height * scale).toInt().coerceAtLeast(0)

        if (storage.width != w || storage.height != h) {
            storage.allocPixelsFlags(ImageInfo.makeS32(w, h, ColorAlphaType.PREMUL), false)
        }

        canvas = Canvas(storage, SurfaceProps(pixelGeometry = skiaSwingLayer.pixelGeometry))
    }

    override fun flush() {
        val scale = skiaSwingLayer.graphicsConfiguration.defaultTransform.scaleX.toFloat()
        val w = (skiaSwingLayer.width * scale).toInt().coerceAtLeast(0)
        val h = (skiaSwingLayer.height * scale).toInt().coerceAtLeast(0)


        val bytes = storage.readPixels(storage.imageInfo, (w * 4), 0, 0)
        if (bytes != null) {
            val buffer = DataBufferByte(bytes, bytes.size)
            raster = Raster.createInterleavedRaster(
                buffer,
                w,
                h,
                w * 4, 4,
                intArrayOf(2, 1, 0, 3), // BGRA order
                null
            )
            image = BufferedImage(colorModel, raster!!, false, null)
            val g = graphics ?: return

            g.setColor(Color(0, 0, 0, 0))
            g.clearRect(0, 0, w, h)
            g.drawImage(image!!, 0, 0, skiaSwingLayer.width, skiaSwingLayer.height, null)
        }
    }
}
