package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.Rect
import org.jetbrains.skia.impl.ArrayInteropDecoder
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.interopScope

class TextBox(val rect: Rect, val direction: Direction) {


    constructor(l: Float, t: Float, r: Float, b: Float, direction: Int) : this(
        Rect.makeLTRB(l, t, r, b),
        Direction.entries[direction]
    )

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is TextBox) return false
        if (this.rect != other.rect) return false
        return this.direction == other.direction
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + rect.hashCode()
        result = result * PRIME + direction.hashCode()
        return result
    }

    override fun toString(): String {
        return "TextBox(_rect=$rect, _direction=$direction)"
    }

    internal companion object : ArrayInteropDecoder<TextBox> {
        override fun getArrayElement(array: InteropPointer, index: Int): TextBox {
            val rect = FloatArray(4)
            val direction = IntArray(1)
            interopScope {
                val rectPtr = toInterop(rect)
                val directionPtr = toInterop(direction)
                TextBox_nGetArrayElement(array, index, rectPtr, directionPtr)
                rectPtr.fromInterop(rect)
                directionPtr.fromInterop(direction)
            }
            return TextBox(rect[0], rect[1], rect[2], rect[3], direction[0])
        }
        override fun getArraySize(array: InteropPointer) = TextBox_nGetArraySize(array)
        override fun disposeArray(array: InteropPointer) = TextBox_nDisposeArray(array)
    }
}

// <editor-fold defaultstate="collapsed" desc="Native External Functions">
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextBox__1nGetArraySize")
// No ForceNativeThreadState: returns the stored element count of the box list, so it is short and cannot block.
// Native call path: vector<TextBox>::size
private external fun TextBox_nGetArraySize(array: InteropPointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextBox__1nDisposeArray")
// No ForceNativeThreadState: frees a list of boxes that owns only plain rectangles and enums, so it is short and cannot
// block.
// Native call path: vector<TextBox>::~vector
private external fun TextBox_nDisposeArray(array: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextBox__1nGetArrayElement")
// No ForceNativeThreadState: copies one box's rectangle and direction into caller arrays, so it is short and cannot
// block.
// Native call path: vector<TextBox>::at
private external fun TextBox_nGetArrayElement(array: InteropPointer, index: Int, rectArray: InteropPointer, directionArray: InteropPointer)
// </editor-fold>