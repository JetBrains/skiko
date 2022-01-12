package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.BufferUtil
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.*
import java.awt.event.*
import java.awt.event.KeyEvent.*
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

fun toSkikoEvent(event: MouseEvent): SkikoPointerEvent {
    return SkikoPointerEvent(
        event.x.toDouble(),
        event.y.toDouble(),
        toSkikoMouseButtons(event.modifiersEx),
        toSkikoModifiers(event.modifiersEx),
        when(event.id) {
            MouseEvent.MOUSE_PRESSED -> SkikoPointerEventKind.DOWN
            MouseEvent.MOUSE_RELEASED -> SkikoPointerEventKind.UP
            MouseEvent.MOUSE_DRAGGED -> SkikoPointerEventKind.DRAG
            MouseEvent.MOUSE_MOVED -> SkikoPointerEventKind.MOVE
            MouseEvent.MOUSE_ENTERED -> SkikoPointerEventKind.ENTER
            MouseEvent.MOUSE_EXITED -> SkikoPointerEventKind.EXIT
            else -> SkikoPointerEventKind.UNKNOWN
        },
        event
    )
}

fun toSkikoEvent(event: MouseWheelEvent): SkikoPointerEvent {
    val scrollAmount = event.getPreciseWheelRotation()
    val modifiers = toSkikoModifiers(event.modifiersEx)
    val isShiftPressed = modifiers.has(SkikoInputModifiers.SHIFT)
    val x = if (isShiftPressed) scrollAmount else 0.0
    val y = if (isShiftPressed) 0.0 else scrollAmount
    return SkikoPointerEvent(
        x,
        y,
        toSkikoMouseButtons(event.modifiersEx),
        modifiers,
        when(event.id) {
            MouseEvent.MOUSE_WHEEL-> SkikoPointerEventKind.SCROLL
            else -> SkikoPointerEventKind.UNKNOWN
        },
        event
    )
}

fun toSkikoEvent(event: KeyEvent): SkikoKeyboardEvent {
    var key = event.keyCode
    val side = event.getKeyLocation()
    if (side == KEY_LOCATION_RIGHT) {
        if (
            key == SkikoKey.KEY_LEFT_CONTROL.value ||
            key == SkikoKey.KEY_LEFT_SHIFT.value ||
            key == SkikoKey.KEY_LEFT_META.value
        )
        key = key.or(0x80000000.toInt())
    }
    if (side == KEY_LOCATION_NUMPAD) {
        if (key == SkikoKey.KEY_ENTER.value) {
            key = key.or(0x80000000.toInt())
        }
    }
    return SkikoKeyboardEvent(
        SkikoKey.valueOf(key),
        toSkikoModifiers(event.modifiersEx),
        when(event.id) {
            KeyEvent.KEY_PRESSED -> SkikoKeyboardEventKind.DOWN
            KeyEvent.KEY_RELEASED -> SkikoKeyboardEventKind.UP
            else -> SkikoKeyboardEventKind.UNKNOWN
        },
        event
    )
}

fun toSkikoTypeEvent(event: KeyEvent): SkikoInputEvent {
    return SkikoInputEvent(
        event.keyChar.toString(),
        SkikoKeyboardEventKind.TYPE,
        event
    )
}

fun toSkikoEvent(event: InputMethodEvent): SkikoInputEvent {
    return SkikoInputEvent(
        "",
        SkikoKeyboardEventKind.TYPE,
        event
    )
}

private fun toSkikoMouseButtons(buttons: Int): SkikoMouseButtons {
    var result = 0
    if (buttons and InputEvent.BUTTON1_DOWN_MASK != 0) {
        result = result.or(SkikoMouseButtons.LEFT.value)
    }
    if (buttons and InputEvent.BUTTON2_DOWN_MASK != 0) {
        result = result.or(SkikoMouseButtons.RIGHT.value)
    }
    if (buttons and InputEvent.BUTTON3_DOWN_MASK != 0) {
        result = result.or(SkikoMouseButtons.MIDDLE.value)
    }
    return SkikoMouseButtons(result)
}

private fun toSkikoModifiers(modifiers: Int): SkikoInputModifiers {
    var result = 0
    if (modifiers and InputEvent.ALT_DOWN_MASK != 0) {
        result = result.or(SkikoInputModifiers.ALT.value)
    }
    if (modifiers and InputEvent.SHIFT_DOWN_MASK != 0) {
        result = result.or(SkikoInputModifiers.SHIFT.value)
    }
    if (modifiers and InputEvent.CTRL_DOWN_MASK != 0) {
        result = result.or(SkikoInputModifiers.CONTROL.value)
    }
    if (modifiers and InputEvent.META_DOWN_MASK != 0) {
        result = result.or(SkikoInputModifiers.META.value)
    }
    return SkikoInputModifiers(result)
}

private fun toSkikoKey(event: KeyEvent): Int {
    var key = event.keyCode
    val side = event.getKeyLocation()
    if (side == KEY_LOCATION_RIGHT) {
        if (
            key == SkikoKey.KEY_LEFT_CONTROL.value ||
            key == SkikoKey.KEY_LEFT_SHIFT.value ||
            key == SkikoKey.KEY_LEFT_META.value
        )
        key = key.or(0x80000000.toInt())
    }
    if (side == KEY_LOCATION_NUMPAD) {
        if (key == SkikoKey.KEY_ENTER.value) {
            key = key.or(0x80000000.toInt())
        }
    }
    return key
}

suspend fun java.awt.Font.toSkikoTypeface(): Typeface? {
    val file = AwtFontManager.DEFAULT.findFontFile(this) ?: return null
    val data = file.readBytes()
    return Typeface.makeFromData(Data.makeFromBytes(data))
}