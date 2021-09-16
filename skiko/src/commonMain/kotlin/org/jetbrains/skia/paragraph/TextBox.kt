package org.jetbrains.skia.paragraph

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.*

class TextBox(val rect: Rect, direction: Direction) {
    val _direction: Direction

    constructor(l: Float, t: Float, r: Float, b: Float, direction: Int) : this(
        Rect.makeLTRB(l, t, r, b),
        Direction.values().get(direction)
    )

    val direction: Direction
        get() = _direction

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is TextBox) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        val `this$_rect`: Any = rect
        val `other$_rect`: Any = other.rect
        if (if (`this$_rect` == null) `other$_rect` != null else `this$_rect` != `other$_rect`) return false
        val `this$_direction`: Any = direction
        val `other$_direction`: Any = other.direction
        return !if (`this$_direction` == null) true else `this$_direction` != `other$_direction`
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is TextBox
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$_rect`: Any = rect
        result = result * PRIME + (`$_rect`?.hashCode() ?: 43)
        val `$_direction`: Any = direction
        result = result * PRIME + (`$_direction`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "TextBox(_rect=" + rect + ", _direction=" + direction + ")"
    }

    init {
        _direction = direction
    }

    companion object : ArrayInteropDecoder<TextBox> {
        override fun getArrayElement(array: InteropPointer, index: Int): TextBox {
            val rect = FloatArray(4)
            val direction = IntArray(1)
            interopScope {
                TextBox_nGetArrayElement(array, index, toInterop(rect), toInterop(direction))
            }
            return TextBox(rect[0], rect[1], rect[2], rect[3], direction[0])
        }
        override fun getArraySize(array: InteropPointer) = TextBox_nGetArraySize(array)
        override fun disposeArray(array: InteropPointer) = TextBox_nDisposeArray(array)
    }
}

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextBox__1nGetArraySize")
private external fun TextBox_nGetArraySize(array: InteropPointer): Int
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextBox__1nDisposeArray")
private external fun TextBox_nDisposeArray(array: InteropPointer)
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextBox__1nGetArrayElement")
private external fun TextBox_nGetArrayElement(array: InteropPointer, index: Int, rectArray: InteropPointer, directionArray: InteropPointer)
