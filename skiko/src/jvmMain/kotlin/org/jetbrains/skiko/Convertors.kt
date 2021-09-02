package org.jetbrains.skiko

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ComponentColorModel
import java.awt.image.DataBuffer
import java.awt.image.Raster
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO

private class DirectDataBuffer(val backing: ByteBuffer): DataBuffer(TYPE_BYTE, backing.limit()) {
    override fun getElem(bank: Int, index: Int): Int {
        return backing[index].toInt()
    }
    override fun setElem(bank: Int, index: Int, value: Int) {
        throw UnsupportedOperationException("no write access")
    }
}

fun Bitmap.toBufferedImage(): BufferedImage {
    val pixels = this.peekPixels()
    val order = when (this.colorInfo.colorType) {
        ColorType.RGB_888X -> intArrayOf(0, 1, 2, 3)
        ColorType.BGRA_8888 -> intArrayOf(2, 1, 0, 3)
        else -> throw UnsupportedOperationException("unsupported color type ${this.colorInfo.colorType}")
    }
    val raster = Raster.createInterleavedRaster(
        DirectDataBuffer(pixels!!),
        this.width,
        this.height,
        this.width * 4,
        4,
        order,
        null
    )
    val colorModel = ComponentColorModel(
        ColorSpace.getInstance(ColorSpace.CS_sRGB),
        true,
        false,
        Transparency.TRANSLUCENT,
        DataBuffer.TYPE_BYTE
    )
   return BufferedImage(colorModel, raster!!, false, null)
}

fun BufferedImage.toBitmap(): Bitmap {
    return Bitmap.makeFromImage(this.toImage())
}

fun BufferedImage.toImage(): Image {
    val bos = ByteArrayOutputStream()
    ImageIO.write(this, "png", bos)
    val data = bos.toByteArray()
    return Image.makeFromEncoded(data)
}