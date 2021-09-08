package org.jetbrains.skia.shaper

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

class FontRun(internal val end: Int, internal val font: Font) {
    private fun _getFontPtr(): NativePointer {
        return try {
            getPtr(font)
        } finally {
            reachabilityBarrier(font)
        }
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is FontRun) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (end != other.end) return false
        val `this$_font`: Any = font
        val `other$_font`: Any = other.font
        return `this$_font` == `other$_font`
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is FontRun
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + end
        val `$_font`: Any = font
        result = result * PRIME + `$_font`.hashCode()
        return result
    }

    override fun toString(): String {
        return "FontRun(_end=$end, _font=$font)"
    }
}