package org.jetbrains.skiko

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Image
import org.jetbrains.skia.impl.BufferUtil
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.*
import java.awt.event.*
import java.nio.ByteBuffer

private class DirectDataBuffer(val backing: ByteBuffer): DataBuffer(TYPE_BYTE, backing.limit()) {
    override fun getElem(bank: Int, index: Int): Int {
        return backing[index].toInt()
    }
    override fun setElem(bank: Int, index: Int, value: Int) {
        throw UnsupportedOperationException("no write access")
    }
}

fun Bitmap.toBufferedImage(): BufferedImage {
    val pixelsNativePointer = this.peekPixels()!!.addr
    val pixelsBuffer = BufferUtil.getByteBufferFromPointer(pixelsNativePointer, this.rowBytes * this.height)

    val order = when (this.colorInfo.colorType) {
        ColorType.RGB_888X -> intArrayOf(0, 1, 2, 3)
        ColorType.BGRA_8888 -> intArrayOf(2, 1, 0, 3)
        else -> throw UnsupportedOperationException("unsupported color type ${this.colorInfo.colorType}")
    }
    val raster = Raster.createInterleavedRaster(
        DirectDataBuffer(pixelsBuffer),
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
    val bytesPerPixel = 4
    val pixels = ByteArray(width * height * bytesPerPixel)

    var k = 0
    for (y in 0 until height) {
        for (x in 0 until width) {
            val argb = getRGB(x, y)
            val a = (argb shr 24) and 0xff
            val r = (argb shr 16) and 0xff
            val g = (argb shr 8) and 0xff
            val b = (argb shr 0) and 0xff
            pixels[k++] = b.toByte()
            pixels[k++] = g.toByte()
            pixels[k++] = r.toByte()
            pixels[k++] = a.toByte()
        }
    }

    val bitmap = Bitmap()
    bitmap.allocPixels(ImageInfo.makeS32(width, height, ColorAlphaType.UNPREMUL))
    bitmap.installPixels(pixels)
    return bitmap
}

fun BufferedImage.toImage(): Image {
    return Image.makeFromBitmap(toBitmap())
}

fun toSkikoEvent(e: MouseEvent): SkikoPointerEvent {
    return SkikoPointerEvent(
        e.x.toDouble(),
        e.y.toDouble(),
        when(e.button) {
            MouseEvent.BUTTON1 -> SkikoMouseButtons.LEFT
            MouseEvent.BUTTON2 -> SkikoMouseButtons.RIGHT
            MouseEvent.BUTTON3 -> SkikoMouseButtons.MIDDLE
            else -> SkikoMouseButtons.NONE
        },
        when(e.id) {
            MouseEvent.MOUSE_PRESSED -> SkikoPointerEventKind.DOWN
            MouseEvent.MOUSE_RELEASED -> SkikoPointerEventKind.UP
            MouseEvent.MOUSE_DRAGGED -> SkikoPointerEventKind.DRAG
            MouseEvent.MOUSE_MOVED -> SkikoPointerEventKind.MOVE
            MouseEvent.MOUSE_ENTERED -> SkikoPointerEventKind.ENTER
            MouseEvent.MOUSE_EXITED -> SkikoPointerEventKind.EXIT
            else -> SkikoPointerEventKind.UNKNOWN
        },
        e
    )
}

fun toSkikoEvent(e: MouseWheelEvent): SkikoPointerEvent {
    return SkikoPointerEvent(
        e.x.toDouble(),
        e.y.toDouble(),
        SkikoMouseButtons.NONE,
        when(e.id) {
            MouseEvent.MOUSE_WHEEL-> SkikoPointerEventKind.SCROLL
            else -> SkikoPointerEventKind.UNKNOWN
        },
        e
    )
}

fun toSkikoEvent(e: KeyEvent): SkikoKeyboardEvent {
    return SkikoKeyboardEvent(
        e.keyCode,
        when(e.id) {
            KeyEvent.KEY_PRESSED -> SkikoKeyboardEventKind.DOWN
            KeyEvent.KEY_RELEASED -> SkikoKeyboardEventKind.UP
            KeyEvent.KEY_TYPED -> SkikoKeyboardEventKind.TYPE
            else -> SkikoKeyboardEventKind.UNKNOWN
        },
        e
    )
}

fun toSkikoEvent(e: InputMethodEvent): SkikoInputEvent {
    return SkikoInputEvent(
        "",
        e
    )
}
