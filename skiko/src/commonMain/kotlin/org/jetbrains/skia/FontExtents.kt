package org.jetbrains.skia

import kotlin.math.abs

class FontExtents(val ascender: Float, val descender: Float, val lineGap: Float) {
    val ascenderAbs: Float
        get() = abs(ascender)
    val lineHeight: Float
        get() = -ascender + descender + lineGap

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is FontExtents) return false
        if (ascender.compareTo(other.ascender) != 0) return false
        if (descender.compareTo(other.descender) != 0) return false
        return lineGap.compareTo(other.lineGap) == 0
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + ascender.toBits()
        result = result * PRIME + descender.toBits()
        result = result * PRIME + lineGap.toBits()
        return result
    }

    override fun toString(): String {
        return "FontExtents(_ascender=$ascender, _descender=$descender, _lineGap=$lineGap)"
    }
}