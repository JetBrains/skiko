package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.shaper.*
import kotlin.jvm.JvmStatic

class TextLine internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        fun make(text: String?, font: Font?): TextLine {
            return make(text, font, ShapingOptions.DEFAULT)
        }

        fun make(text: String?, font: Font?, opts: ShapingOptions?): TextLine {
            return Shaper.makeShapeDontWrapOrReorder().use { shaper -> shaper.shapeLine(text, font, opts!!) }
        }

        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nGetAscent(ptr: Long): Float
        @JvmStatic external fun _nGetCapHeight(ptr: Long): Float
        @JvmStatic external fun _nGetXHeight(ptr: Long): Float
        @JvmStatic external fun _nGetDescent(ptr: Long): Float
        @JvmStatic external fun _nGetLeading(ptr: Long): Float
        @JvmStatic external fun _nGetWidth(ptr: Long): Float
        @JvmStatic external fun _nGetHeight(ptr: Long): Float
        @JvmStatic external fun _nGetTextBlob(ptr: Long): Long
        @JvmStatic external fun _nGetGlyphs(ptr: Long): ShortArray
        @JvmStatic external fun _nGetPositions(ptr: Long): FloatArray
        @JvmStatic external fun _nGetRunPositions(ptr: Long): FloatArray?
        @JvmStatic external fun _nGetBreakPositions(ptr: Long): FloatArray?
        @JvmStatic external fun _nGetBreakOffsets(ptr: Long): IntArray?
        @JvmStatic external fun _nGetOffsetAtCoord(ptr: Long, x: Float): Int
        @JvmStatic external fun _nGetLeftOffsetAtCoord(ptr: Long, x: Float): Int
        @JvmStatic external fun _nGetCoordAtOffset(ptr: Long, offset: Int): Float

        init {
            Library.staticLoad()
        }
    }

    private object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    val ascent: Float
        get() {
            Stats.onNativeCall()
            return try {
                _nGetAscent(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }
    val capHeight: Float
        get() {
            Stats.onNativeCall()
            return try {
                _nGetCapHeight(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }
    val xHeight: Float
        get() {
            Stats.onNativeCall()
            return try {
                _nGetXHeight(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }
    val descent: Float
        get() {
            Stats.onNativeCall()
            return try {
                _nGetDescent(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }
    val leading: Float
        get() {
            Stats.onNativeCall()
            return try {
                _nGetLeading(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }
    val width: Float
        get() {
            Stats.onNativeCall()
            return try {
                _nGetWidth(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }
    val height: Float
        get() {
            Stats.onNativeCall()
            return try {
                _nGetHeight(_ptr)
            } finally {
                reachabilityBarrier(this)
            }
        }
    val textBlob: TextBlob?
        get() {
            Stats.onNativeCall()
            return try {
                val res = _nGetTextBlob(_ptr)
                if (res == 0L) null else TextBlob(res)
            } finally {
                reachabilityBarrier(this)
            }
        }
    val glyphs: ShortArray
        get() {
            Stats.onNativeCall()
            return try {
                _nGetGlyphs(_ptr)
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
                _nGetPositions(_ptr)
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
            _nGetOffsetAtCoord(_ptr, x)
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
            _nGetLeftOffsetAtCoord(_ptr, x)
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
            _nGetCoordAtOffset(_ptr, offset)
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