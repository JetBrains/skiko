package org.jetbrains.skia

import org.jetbrains.skia.impl.Library
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.impl.withResult
import org.jetbrains.skia.shaper.Shaper
import org.jetbrains.skia.shaper.ShapingOptions


class TextLine internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        fun make(text: String?, font: Font?): TextLine {
            return make(text, font, ShapingOptions.DEFAULT)
        }

        fun make(text: String?, font: Font?, opts: ShapingOptions?): TextLine {
            return Shaper.makeShapeDontWrapOrReorder().use { shaper -> shaper.shapeLine(text, font, opts!!) }
        }

        init {
            Library.staticLoad()
        }
    }

    private object _FinalizerHolder {
        val PTR = TextLine_nGetFinalizer()
    }

    /**
     * distance to reserve above baseline, typically negative
     */
    val ascent: Float
        get() {
            Stats.onNativeCall()
            return try {
                TextLine_nGetAscent(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }

    /**
     * height of an upper-case letter, zero if unknown, typically negative
     */
    val capHeight: Float
        get() {
            Stats.onNativeCall()
            return try {
                TextLine_nGetCapHeight(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }

    /**
     * height of lower-case 'x', zero if unknown, typically negative
     */
    val xHeight: Float
        get() {
            Stats.onNativeCall()
            return try {
                TextLine_nGetXHeight(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }

    /**
     * distance to reserve below baseline, typically positive
     */
    val descent: Float
        get() {
            Stats.onNativeCall()
            return try {
                TextLine_nGetDescent(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }

    /**
     * distance to add between lines, typically positive or zero
     */
    val leading: Float
        get() {
            Stats.onNativeCall()
            return try {
                TextLine_nGetLeading(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }

    val width: Float
        get() {
            Stats.onNativeCall()
            return try {
                TextLine_nGetWidth(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }

    val height: Float
        get() {
            Stats.onNativeCall()
            return try {
                TextLine_nGetHeight(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }

    val textBlob: TextBlob?
        get() {
            Stats.onNativeCall()
            return try {
                val res = TextLine_nGetTextBlob(_ptr)
                if (res == NullPointer) null else TextBlob(res)
            } finally {
                reachabilityBarrier(this)
            }
        }

    val glyphs: ShortArray
        get() {
            Stats.onNativeCall()
            return try {
                val length = glyphsLength
                withResult(ShortArray(length)) {
                    TextLine_nGetGlyphs(_ptr, it, length)
                }
            } finally {
                reachabilityBarrier(this)
            }
        }

    internal val glyphsLength: Int
        get() {
            Stats.onNativeCall()
            return try {
                TextLine_nGetGlyphsLength(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }

    /**
     * @return  [x0, y0, x1, y1, ...]
     */
    val positions: FloatArray
        get() {
            Stats.onNativeCall()
            return try {
                withResult(FloatArray(glyphsLength * 2)) {
                    TextLine_nGetPositions(_ptr, it)
                }
            } finally {
                reachabilityBarrier(this)
            }
        }

    internal val runPositions: FloatArray?
        get() {
            Stats.onNativeCall()
            return try {
                withResult(FloatArray(TextLine_nGetRunPositionsCount(_ptr))) {
                    TextLine_nGetRunPositions(_ptr, it)
                }
            } finally {
                reachabilityBarrier(this)
            }
        }

    internal val breakPositions: FloatArray?
        get() {
            Stats.onNativeCall()
            return try {
                withResult(FloatArray(TextLine_nGetBreakPositionsCount(_ptr))) {
                    TextLine_nGetBreakPositions(_ptr, it)
                }
            } finally {
                reachabilityBarrier(this)
            }
        }

    internal val breakOffsets: IntArray
        get() {
            Stats.onNativeCall()
            return try {
                withResult(IntArray(TextLine_nGetBreakOffsetsCount(_ptr))) {
                    TextLine_nGetBreakOffsets(_ptr, it)
                }
            } finally {
                reachabilityBarrier(this)
            }
        }

    /**
     * @param x  coordinate in px
     * @return   UTF-16 offset of glyph
     */
    fun getOffsetAtCoord(x: Float): Int {
        return try {
            Stats.onNativeCall()
            TextLine_nGetOffsetAtCoord(_ptr, x)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * @param x  coordinate in px
     * @return   UTF-16 offset of glyph strictly left of coord
     */
    fun getLeftOffsetAtCoord(x: Float): Int {
        return try {
            Stats.onNativeCall()
            TextLine_nGetLeftOffsetAtCoord(_ptr, x)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     * @param offset  UTF-16 character offset
     * @return        glyph coordinate
     */
    fun getCoordAtOffset(offset: Int): Float {
        return try {
            Stats.onNativeCall()
            TextLine_nGetCoordAtOffset(_ptr, offset)
        } finally {
            reachabilityBarrier(this)
        }
    }

    /**
     *
     * Returns the number of intervals that intersect bounds.
     * bounds describes a pair of lines parallel to the text advance.
     * The return array size is a multiple of two, and is at most twice the number of glyphs in
     * the the blob.
     *
     * @param lowerBound lower line parallel to the advance
     * @param upperBound upper line parallel to the advance
     * @return           intersections; may be null
     */
    fun getIntercepts(lowerBound: Float, upperBound: Float): FloatArray? {
        return getIntercepts(lowerBound, upperBound, null)
    }

    /**
     *
     * Returns the number of intervals that intersect bounds.
     * bounds describes a pair of lines parallel to the text advance.
     * The return array size is a multiple of two, and is at most twice the number of glyphs in
     * the the blob.
     *
     * @param lowerBound lower line parallel to the advance
     * @param upperBound upper line parallel to the advance
     * @param paint      specifies stroking, PathEffect that affects the result; may be null
     * @return           intersections; may be null
     */
    fun getIntercepts(lowerBound: Float, upperBound: Float, paint: Paint?): FloatArray? {
        try {
            textBlob!!.use { blob -> return blob.getIntercepts(lowerBound, upperBound, paint) }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
        }
    }
}