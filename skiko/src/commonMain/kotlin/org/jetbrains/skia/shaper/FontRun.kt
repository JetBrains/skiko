package org.jetbrains.skia.shaper

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.nativeCall
import org.jetbrains.skia.impl.pointerOrNullPointer

class FontRun(internal val end: Int, internal val font: Font) {
    private fun _getFontPtr() = nativeCall(font) { it.pointerOrNullPointer }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is FontRun) return false
        if (end != other.end) return false
        return this.font == other.font
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + end
        result = result * PRIME + font.hashCode()
        return result
    }

    override fun toString(): String {
        return "FontRun(_end=$end, _font=$font)"
    }
}