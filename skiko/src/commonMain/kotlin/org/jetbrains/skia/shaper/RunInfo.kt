package org.jetbrains.skia.shaper

import org.jetbrains.skia.*

class RunInfo(
    var _fontPtr: Long, val bidiLevel: Int, val advanceX: Float, val advanceY: Float, val glyphCount: Long,
    /**
     * WARN does not work in Shaper.makeCoreText https://bugs.chromium.org/p/skia/issues/detail?id=10899
     */
    val rangeBegin: Int,
    /**
     * WARN does not work in Shaper.makeCoreText https://bugs.chromium.org/p/skia/issues/detail?id=10899
     */
    val rangeSize: Int
) {
    /**
     * WARN does not work in Shaper.makeCoreText https://bugs.chromium.org/p/skia/issues/detail?id=10899
     */
    /**
     * WARN does not work in Shaper.makeCoreText https://bugs.chromium.org/p/skia/issues/detail?id=10899
     */
    val advance: Point
        get() = org.jetbrains.skia.Point(advanceX, advanceY)

    /**
     * WARN does not work in Shaper.makeCoreText https://bugs.chromium.org/p/skia/issues/detail?id=10899
     */
    val rangeEnd: Int
        get() = rangeBegin + rangeSize
    val font: Font
        get() {
            check(_fontPtr != 0L) { "getFont() is only valid inside RunHandler callbacks" }
            return Font.Companion.makeClone(_fontPtr)
        }

    fun setFontPtr(_fontPtr: Long): RunInfo {
        this._fontPtr = _fontPtr
        return this
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is RunInfo) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (_fontPtr != other._fontPtr) return false
        if (bidiLevel != other.bidiLevel) return false
        if (advanceX.compareTo(other.advanceX) != 0) return false
        if (advanceY.compareTo(other.advanceY) != 0) return false
        if (glyphCount != other.glyphCount) return false
        if (rangeBegin != other.rangeBegin) return false
        return rangeSize == other.rangeSize
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is RunInfo
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$_fontPtr` = _fontPtr
        result = result * PRIME + (`$_fontPtr` ushr 32 xor `$_fontPtr`).toInt()
        result = result * PRIME + bidiLevel
        result = result * PRIME + advanceX.toBits()
        result = result * PRIME + advanceY.toBits()
        val `$_glyphCount` = glyphCount
        result = result * PRIME + (`$_glyphCount` ushr 32 xor `$_glyphCount`).toInt()
        result = result * PRIME + rangeBegin
        result = result * PRIME + rangeSize
        return result
    }

    override fun toString(): String {
        return "RunInfo(_fontPtr=$_fontPtr, _bidiLevel=$bidiLevel, _advanceX=$advanceX, _advanceY=$advanceY, _glyphCount=$glyphCount, _rangeBegin=$rangeBegin, _rangeSize=$rangeSize)"
    }
}