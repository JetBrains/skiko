package org.jetbrains.skia.shaper

import org.jetbrains.skia.Font
import org.jetbrains.skia.Point
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.NativePointer

class RunInfo(
    var _fontPtr: NativePointer, val bidiLevel: Int, val advanceX: Float, val advanceY: Float, val glyphCount: Int,
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
            check(_fontPtr != Native.NullPointer) { "getFont() is only valid inside RunHandler callbacks" }
            return Font.makeClone(_fontPtr)
        }

    fun setFontPtr(_fontPtr: NativePointer): RunInfo {
        this._fontPtr = _fontPtr
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is RunInfo) return false
        if (_fontPtr != other._fontPtr) return false
        if (bidiLevel != other.bidiLevel) return false
        if (advanceX.compareTo(other.advanceX) != 0) return false
        if (advanceY.compareTo(other.advanceY) != 0) return false
        if (glyphCount != other.glyphCount) return false
        if (rangeBegin != other.rangeBegin) return false
        return rangeSize == other.rangeSize
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        // TODO("Shagen: COMMENT OUT BEFORE ANY ACTUAL USAGE")
        //result = result * PRIME + (fontPtr ushr 32 xor fontPtr).toInt()
        result = result * PRIME + bidiLevel
        result = result * PRIME + advanceX.toBits()
        result = result * PRIME + advanceY.toBits()
        //result = result * PRIME + (glyphCount ushr 32 xor glyphCount).toInt()
        result = result * PRIME + rangeBegin
        result = result * PRIME + rangeSize
        return result
    }

    override fun toString(): String {
        return "RunInfo(_fontPtr=$_fontPtr, _bidiLevel=$bidiLevel, _advanceX=$advanceX, _advanceY=$advanceY, _glyphCount=$glyphCount, _rangeBegin=$rangeBegin, _rangeSize=$rangeSize)"
    }
}