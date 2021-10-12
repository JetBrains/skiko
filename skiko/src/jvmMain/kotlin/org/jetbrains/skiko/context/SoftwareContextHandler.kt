package org.jetbrains.skiko.context

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Picture
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.hostOs
import org.jetbrains.skiko.OS
import java.awt.Transparency
import java.awt.Color
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ComponentColorModel
import java.awt.image.DataBuffer
import java.awt.image.DataBufferByte
import java.awt.image.Raster
import java.awt.image.WritableRaster

internal class SoftwareContextHandler(layer: SkiaLayer) : ContextHandler(layer) {
    override val clearColor = if (layer.transparency && hostOs == OS.MacOS) 0 else -1

    val colorModel = ComponentColorModel(
        ColorSpace.getInstance(ColorSpace.CS_sRGB),
        true,
        false,
        Transparency.TRANSLUCENT,
        DataBuffer.TYPE_BYTE
    )
    val storage = Bitmap()
    var image: BufferedImage? = null
    var imageData: ByteArray? = null
    var raster: WritableRaster? = null
    var isInited = false

    override fun initContext(): Boolean {
        // Raster does not need context
        if (!isInited) {
            if (System.getProperty("skiko.hardwareInfo.enabled") == "true") {
                println(rendererInfo())
            }
            isInited = true
        }
        return isInited
    }

    override fun initCanvas() {
        disposeCanvas()

        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)

        if (storage.width != w || storage.height != h) {
            storage.allocPixelsFlags(ImageInfo.makeS32(w, h, ColorAlphaType.PREMUL), false)
        }

        canvas = Canvas(storage)
    }

    override fun drawOnCanvas(picture: Picture) {
        super.drawOnCanvas(picture)

        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)


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
            val graphics = layer.backedLayer.getGraphics()
            if (!layer.fullscreen && layer.transparency && hostOs == OS.MacOS) {
                graphics?.setColor(Color(0, 0, 0, 0))
                graphics?.clearRect(0, 0, w, h)
            }
            graphics?.drawImage(image!!, 0, 0, layer.width, layer.height, null)
        }
    }

    override fun flush() {
        // Raster does not need to flush canvas
    }
}
