package org.jetbrains.skiko

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.BufferUtil
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.event.*
import java.awt.event.KeyEvent.*
import java.awt.image.BufferedImage
import java.awt.image.ComponentColorModel
import java.awt.image.DataBuffer
import java.awt.image.Raster
import java.nio.ByteBuffer

private class DirectDataBuffer(val backing: ByteBuffer) : DataBuffer(TYPE_BYTE, backing.limit()) {
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

private val MouseEventButton4 get() = 4
private val MouseEventButton5 get() = 5

fun toSkikoEvent(event: MouseEvent): SkikoPointerEvent {
    return SkikoPointerEvent(
        x = event.x.toDouble(),
        y = event.y.toDouble(),
        pressedButtons = toSkikoPressedMouseButtons(event),
        button = toSkikoMouseButton(event),
        modifiers = toSkikoModifiers(event.modifiersEx),
        kind = when (event.id) {
            MouseEvent.MOUSE_PRESSED -> SkikoPointerEventKind.DOWN
            MouseEvent.MOUSE_RELEASED -> SkikoPointerEventKind.UP
            MouseEvent.MOUSE_DRAGGED -> SkikoPointerEventKind.DRAG
            MouseEvent.MOUSE_MOVED -> SkikoPointerEventKind.MOVE
            MouseEvent.MOUSE_ENTERED -> SkikoPointerEventKind.ENTER
            MouseEvent.MOUSE_EXITED -> SkikoPointerEventKind.EXIT
            else -> SkikoPointerEventKind.UNKNOWN
        },
        timestamp = event.`when`,
        platform = event
    )
}

fun toSkikoEvent(event: MouseWheelEvent): SkikoPointerEvent {
    val scrollAmount = event.preciseWheelRotation
    val modifiers = toSkikoModifiers(event.modifiersEx)
    val isShiftPressed = modifiers.has(SkikoInputModifiers.SHIFT)
    val deltaX = if (isShiftPressed) scrollAmount else 0.0
    val deltaY = if (isShiftPressed) 0.0 else scrollAmount
    return SkikoPointerEvent(
        x = event.x.toDouble(),
        y = event.y.toDouble(),
        deltaX = deltaX,
        deltaY = deltaY,
        pressedButtons = toSkikoPressedMouseButtons(event),
        button = toSkikoMouseButton(event),
        modifiers = modifiers,
        kind = when (event.id) {
            MouseEvent.MOUSE_WHEEL -> SkikoPointerEventKind.SCROLL
            else -> SkikoPointerEventKind.UNKNOWN
        },
        timestamp = event.`when`,
        platform = event
    )
}

fun toSkikoEvent(event: KeyEvent): SkikoKeyboardEvent {
    return SkikoKeyboardEvent(
        SkikoKey.valueOf(toSkikoKey(event)),
        toSkikoModifiers(event.modifiersEx),
        when (event.id) {
            KEY_PRESSED -> SkikoKeyboardEventKind.DOWN
            KEY_RELEASED -> SkikoKeyboardEventKind.UP
            else -> SkikoKeyboardEventKind.UNKNOWN
        },
        event.`when`,
        event
    )
}

fun toSkikoTypeEvent(typeEvent: KeyEvent, keyEvent: KeyEvent?): SkikoInputEvent {
    var key: Int = -1
    var modifiers = SkikoInputModifiers.EMPTY
    if (keyEvent != null) {
        key = toSkikoKey(keyEvent)
        modifiers = toSkikoModifiers(keyEvent.modifiersEx)
    }
    return SkikoInputEvent(
        typeEvent.keyChar.toString(),
        SkikoKey.valueOf(key),
        modifiers,
        SkikoKeyboardEventKind.TYPE,
        typeEvent
    )
}

fun toSkikoTypeEvent(event: InputMethodEvent, keyEvent: KeyEvent?): SkikoInputEvent {
    var key: Int = -1
    var modifiers = SkikoInputModifiers.EMPTY
    if (keyEvent != null) {
        key = toSkikoKey(keyEvent)
        modifiers = toSkikoModifiers(keyEvent.modifiersEx)
    }
    return SkikoInputEvent(
        "",
        SkikoKey.valueOf(key),
        modifiers,
        SkikoKeyboardEventKind.TYPE,
        event
    )
}

private fun toSkikoPressedMouseButtons(event: MouseEvent): SkikoMouseButtons {
    val mask = event.modifiersEx
    var result = 0
    // We should check [event.button] because of case where [event.modifiersEx] does not provide
    // info about the pressed mouse button when using touchpad on MacOS 12 (AWT only)
    // see: https://youtrack.jetbrains.com/issue/COMPOSE-36
    if (mask and InputEvent.BUTTON1_DOWN_MASK != 0
        || (event.id == MouseEvent.MOUSE_PRESSED && event.button == MouseEvent.BUTTON1)) {
        result = result.or(SkikoMouseButtons.LEFT.value)
    }
    if (mask and InputEvent.BUTTON2_DOWN_MASK != 0
        || (event.id == MouseEvent.MOUSE_PRESSED && event.button == MouseEvent.BUTTON2)) {
        result = result.or(SkikoMouseButtons.MIDDLE.value)
    }
    if (mask and InputEvent.BUTTON3_DOWN_MASK != 0
        || (event.id == MouseEvent.MOUSE_PRESSED && event.button == MouseEvent.BUTTON3)) {
        result = result.or(SkikoMouseButtons.RIGHT.value)
    }
    if (mask and MouseEvent.getMaskForButton(MouseEventButton4) != 0
        || (event.id == MouseEvent.MOUSE_PRESSED && event.button == MouseEventButton4)) {
        result = result.or(SkikoMouseButtons.BUTTON_4.value)
    }
    if (mask and MouseEvent.getMaskForButton(MouseEventButton5) != 0
        || (event.id == MouseEvent.MOUSE_PRESSED && event.button == MouseEventButton5)) {
        result = result.or(SkikoMouseButtons.BUTTON_5.value)
    }
    return SkikoMouseButtons(result)
}

private fun toSkikoMouseButton(event: MouseEvent): SkikoMouseButtons {
    return when (event.button) {
        MouseEvent.BUTTON1 -> SkikoMouseButtons.LEFT
        MouseEvent.BUTTON2 -> SkikoMouseButtons.MIDDLE
        MouseEvent.BUTTON3 -> SkikoMouseButtons.RIGHT
        MouseEventButton4 -> SkikoMouseButtons.BUTTON_4
        MouseEventButton5 -> SkikoMouseButtons.BUTTON_5
        else -> SkikoMouseButtons(event.button)
    }
}

private fun toSkikoModifiers(modifiers: Int): SkikoInputModifiers {
    var result = 0
    if (modifiers and InputEvent.ALT_DOWN_MASK != 0) {
        result = SkikoInputModifiers.ALT.value
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
    val side = event.keyLocation
    if (side == KEY_LOCATION_RIGHT) {
        if (
            key == SkikoKey.KEY_LEFT_CONTROL.platformKeyCode ||
            key == SkikoKey.KEY_LEFT_SHIFT.platformKeyCode ||
            key == SkikoKey.KEY_LEFT_META.platformKeyCode
        ) {
            key = key.or(0x80000000.toInt())
        }
    }
    if (side == KEY_LOCATION_NUMPAD) {
        if (key == SkikoKey.KEY_ENTER.platformKeyCode) {
            key = key.or(0x80000000.toInt())
        }
    }
    return key
}
