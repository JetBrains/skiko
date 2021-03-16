package org.jetbrains.skiko.context

import java.awt.AlphaComposite
import java.awt.color.ColorSpace
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Transparency
import java.awt.image.ComponentColorModel
import java.awt.image.BufferedImage
import java.awt.image.DataBuffer
import java.awt.image.DataBufferByte
import java.awt.image.WritableRaster
import java.awt.image.Raster
import org.jetbrains.skija.Bitmap
import org.jetbrains.skija.ColorAlphaType
import org.jetbrains.skija.Canvas
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skija.ImageInfo
import org.jetbrains.skija.Picture
import org.jetbrains.skiko.redrawer.Redrawer

internal class SoftwareContextHandler(layer: SkiaLayer) : ContextHandler(layer) {
    override val bleachConstant = -1 // it looks like java.awt.Canvas doesn't support transparency

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

    override fun initContext(): Boolean {
        // Raster does not need context
        return true
    }

    override fun initCanvas() {
        dispose()
        
        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)
        
        if (storage.getWidth() != w || storage.getHeight() != h) {
            storage.allocPixelsFlags(ImageInfo.makeS32(w, h, ColorAlphaType.PREMUL), false)
        }

        canvas = Canvas(storage)
    }

    override fun drawOnCanvas(picture: Picture) {
        super.drawOnCanvas(picture)

        val scale = layer.contentScale
        val w = (layer.width * scale).toInt().coerceAtLeast(0)
        val h = (layer.height * scale).toInt().coerceAtLeast(0)


        val bytes = storage.readPixels(storage.getImageInfo(), (w * 4).toLong(), 0, 0)
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
            layer.backedLayer.getGraphics()?.drawImage(image!!, 0, 0, layer.width, layer.height, null)
        }
    }

    override fun flush() {
        // Raster does not need to flush canvas
    }
}
