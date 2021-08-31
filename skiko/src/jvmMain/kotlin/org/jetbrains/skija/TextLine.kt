package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Contract
import org.jetbrains.skija.impl.Library
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Stats
import org.jetbrains.skija.shaper.*
import java.lang.ref.Reference

class TextLine @ApiStatus.Internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @Contract("_, _ -> new")
        fun make(text: String?, font: Font?): TextLine {
            return make(text, font, ShapingOptions.DEFAULT)
        }

        @Contract("_, _, _, _ -> new")
        fun make(text: String?, font: Font?, opts: ShapingOptions?): TextLine {
            Shaper.makeShapeDontWrapOrReorder().use { shaper -> return shaper.shapeLine(text, font, opts!!) }
        }

        @ApiStatus.Internal
        external fun _nGetFinalizer(): Long
        @ApiStatus.Internal
        external fun _nGetAscent(ptr: Long): Float
        @ApiStatus.Internal
        external fun _nGetCapHeight(ptr: Long): Float
        @ApiStatus.Internal
        external fun _nGetXHeight(ptr: Long): Float
        @ApiStatus.Internal
        external fun _nGetDescent(ptr: Long): Float
        @ApiStatus.Internal
        external fun _nGetLeading(ptr: Long): Float
        @ApiStatus.Internal
        external fun _nGetWidth(ptr: Long): Float
        @ApiStatus.Internal
        external fun _nGetHeight(ptr: Long): Float
        @ApiStatus.Internal
        external fun _nGetTextBlob(ptr: Long): Long
        @ApiStatus.Internal
        external fun _nGetGlyphs(ptr: Long): ShortArray
        @ApiStatus.Internal
        external fun _nGetPositions(ptr: Long): FloatArray
        @ApiStatus.Internal
        external fun _nGetRunPositions(ptr: Long): FloatArray?
        @ApiStatus.Internal
        external fun _nGetBreakPositions(ptr: Long): FloatArray?
        @ApiStatus.Internal
        external fun _nGetBreakOffsets(ptr: Long): IntArray?
        @ApiStatus.Internal
        external fun _nGetOffsetAtCoord(ptr: Long, x: Float): Int
        @ApiStatus.Internal
        external fun _nGetLeftOffsetAtCoord(ptr: Long, x: Float): Int
        @ApiStatus.Internal
        external fun _nGetCoordAtOffset(ptr: Long, offset: Int): Float

        init {
            Library.staticLoad()
        }
    }

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    val ascent: Float
        get() {
            Stats.onNativeCall()
            return try {
                _nGetAscent(_ptr)
            } finally {
                Reference.reachabilityFence(this)
            }
        }
    val capHeight: Float
        get() {
            Stats.onNativeCall()
            return try {
                _nGetCapHeight(_ptr)
            } finally {
                Reference.reachabilityFence(this)
            }
        }
    val xHeight: Float
        get() {
            Stats.onNativeCall()
            return try {
                _nGetXHeight(_ptr)
            } finally {
                Reference.reachabilityFence(this)
            }
        }
    val descent: Float
        get() {
            Stats.onNativeCall()
            return try {
                _nGetDescent(_ptr)
            } finally {
                Reference.reachabilityFence(this)
            }
        }
    val leading: Float
        get() {
            Stats.onNativeCall()
            return try {
                _nGetLeading(_ptr)
            } finally {
                Reference.reachabilityFence(this)
            }
        }
    val width: Float
        get() {
            Stats.onNativeCall()
            return try {
                _nGetWidth(_ptr)
            } finally {
                Reference.reachabilityFence(this)
            }
        }
    val height: Float
        get() {
            Stats.onNativeCall()
            return try {
                _nGetHeight(_ptr)
            } finally {
                Reference.reachabilityFence(this)
            }
        }
    val textBlob: TextBlob?
        get() {
            Stats.onNativeCall()
            return try {
                val res = _nGetTextBlob(_ptr)
                if (res == 0L) null else TextBlob(res)
            } finally {
                Reference.reachabilityFence(this)
            }
        }
    val glyphs: ShortArray
        get() {
            Stats.onNativeCall()
            return try {
                _nGetGlyphs(_ptr)
            } finally {
                Reference.reachabilityFence(this)
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
                Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
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
            textBlob.use { blob -> return blob?.getIntercepts(lowerBound, upperBound, paint) }
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(paint)
        }
    }
}