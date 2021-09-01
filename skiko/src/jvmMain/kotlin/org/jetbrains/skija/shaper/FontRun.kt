package org.jetbrains.skija.shaper

import org.jetbrains.skija.*
import org.jetbrains.skija.impl.Native
import java.lang.ref.Reference

class FontRun(internal val end: Int, internal val font: Font) {
    private fun _getFontPtr(): Long {
        return try {
            Native.Companion.getPtr(font)
        } finally {
            Reference.reachabilityFence(font)
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
        return if (if (`this$_font` == null) `other$_font` != null else `this$_font` != `other$_font`) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is FontRun
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + end
        val `$_font`: Any = font
        result = result * PRIME + (`$_font`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "FontRun(_end=" + end + ", _font=" + font + ")"
    }
}