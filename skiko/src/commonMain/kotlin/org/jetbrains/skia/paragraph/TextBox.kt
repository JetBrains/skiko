package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.Rect
import org.jetbrains.skia.impl.ArrayInteropDecoder
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.interopScope

class TextBox(val rect: Rect, direction: Direction) {
    val _direction: Direction

    constructor(l: Float, t: Float, r: Float, b: Float, direction: Int) : this(
        Rect.makeLTRB(l, t, r, b),
        Direction.values().get(direction)
    )

    val direction: Direction
        get() = _direction

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

    init {
        _direction = direction
    }

    companion object : ArrayInteropDecoder<TextBox> {
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

@ExternalSymbolName("org_jetbrains_skia_paragraph_TextBox__1nGetArraySize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextBox__1nGetArraySize")
private external fun TextBox_nGetArraySize(array: InteropPointer): Int
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextBox__1nDisposeArray")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextBox__1nDisposeArray")
private external fun TextBox_nDisposeArray(array: InteropPointer)
@ExternalSymbolName("org_jetbrains_skia_paragraph_TextBox__1nGetArrayElement")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TextBox__1nGetArrayElement")
private external fun TextBox_nGetArrayElement(array: InteropPointer, index: Int, rectArray: InteropPointer, directionArray: InteropPointer)
