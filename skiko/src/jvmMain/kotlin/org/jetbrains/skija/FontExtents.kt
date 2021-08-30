package org.jetbrains.skija

class FontExtents(val ascender: Float, val descender: Float, val lineGap: Float) {
    val ascenderAbs: Float
        get() = Math.abs(ascender)
    val lineHeight: Float
        get() = -ascender + descender + lineGap

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is FontExtents) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (java.lang.Float.compare(ascender, other.ascender) != 0) return false
        if (java.lang.Float.compare(descender, other.descender) != 0) return false
        return if (java.lang.Float.compare(lineGap, other.lineGap) != 0) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is FontExtents
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + java.lang.Float.floatToIntBits(ascender)
        result = result * PRIME + java.lang.Float.floatToIntBits(descender)
        result = result * PRIME + java.lang.Float.floatToIntBits(lineGap)
        return result
    }

    override fun toString(): String {
        return "FontExtents(_ascender=" + ascender + ", _descender=" + descender + ", _lineGap=" + lineGap + ")"
    }
}