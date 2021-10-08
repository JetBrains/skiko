package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.shaper.*

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
                val res = _nGetTextBlob(_ptr)
                if (res == NullPointer) null else TextBlob(res)
            } finally {
                reachabilityBarrier(this)
            }
        }
    val glyphs: ShortArray
        get() {
            Stats.onNativeCall()
            return try {
                TextLine_nGetGlyphs(_ptr)
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
                TextLine_nGetPositions(_ptr)
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


@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetFinalizer")
private external fun TextLine_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetWidth")
private external fun TextLine_nGetWidth(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetHeight")
private external fun TextLine_nGetHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetGlyphs")
private external fun TextLine_nGetGlyphs(ptr: NativePointer): ShortArray

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetPositions")
private external fun TextLine_nGetPositions(ptr: NativePointer): FloatArray

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetAscent")
private external fun _nGetAscent(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetCapHeight")
private external fun _nGetCapHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetXHeight")
private external fun _nGetXHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetDescent")
private external fun _nGetDescent(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetLeading")
private external fun _nGetLeading(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetTextBlob")
private external fun _nGetTextBlob(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetRunPositions")
private external fun _nGetRunPositions(ptr: NativePointer): FloatArray?

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetBreakPositions")
private external fun _nGetBreakPositions(ptr: NativePointer): FloatArray?

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetBreakOffsets")
private external fun _nGetBreakOffsets(ptr: NativePointer): IntArray?

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetOffsetAtCoord")
private external fun _nGetOffsetAtCoord(ptr: NativePointer, x: Float): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetLeftOffsetAtCoord")
private external fun _nGetLeftOffsetAtCoord(ptr: NativePointer, x: Float): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetCoordAtOffset")
private external fun _nGetCoordAtOffset(ptr: NativePointer, offset: Int): Float
