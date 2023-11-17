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

    /**
     * distance to reserve above baseline, typically negative
     */
    val ascent: Float
        get() {
            Stats.onNativeCall()
            return try {
                _nGetAscent(_ptr)
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
                _nGetCapHeight(_ptr)
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
                _nGetXHeight(_ptr)
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
                _nGetDescent(_ptr)
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
                withResult(FloatArray(_nGetRunPositionsCount(_ptr))) {
                    _nGetRunPositions(_ptr, it)
                }
            } finally {
                reachabilityBarrier(this)
            }
        }

    internal val breakPositions: FloatArray?
        get() {
            Stats.onNativeCall()
            return try {
                withResult(FloatArray(_nGetBreakPositionsCount(_ptr))) {
                    _nGetBreakPositions(_ptr, it)
                }
            } finally {
                reachabilityBarrier(this)
            }
        }

    internal val breakOffsets: IntArray
        get() {
            Stats.onNativeCall()
            return try {
                withResult(IntArray(_nGetBreakOffsetsCount(_ptr))) {
                    _nGetBreakOffsets(_ptr, it)
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
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetFinalizer")
private external fun TextLine_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetWidth")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetWidth")
private external fun TextLine_nGetWidth(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetHeight")
private external fun TextLine_nGetHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetGlyphsLength")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetGlyphsLength")
private external fun TextLine_nGetGlyphsLength(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetGlyphs")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetGlyphs")
private external fun TextLine_nGetGlyphs(ptr: NativePointer, resultGlyphs: InteropPointer, resultLength: Int)

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetPositions")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetPositions")
private external fun TextLine_nGetPositions(ptr: NativePointer, resultArray: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetAscent")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetAscent")
private external fun _nGetAscent(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetCapHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetCapHeight")
private external fun _nGetCapHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetXHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetXHeight")
private external fun _nGetXHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetDescent")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetDescent")
private external fun _nGetDescent(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetLeading")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetLeading")
private external fun _nGetLeading(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetTextBlob")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetTextBlob")
private external fun _nGetTextBlob(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetRunPositions")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetRunPositions")
private external fun _nGetRunPositions(ptr: NativePointer, resultArray: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetRunPositionsCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetRunPositionsCount")
private external fun _nGetRunPositionsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetBreakPositionsCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetBreakPositionsCount")
private external fun _nGetBreakPositionsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetBreakPositions")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetBreakPositions")
private external fun _nGetBreakPositions(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetBreakOffsetsCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetBreakOffsetsCount")
private external fun _nGetBreakOffsetsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetBreakOffsets")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetBreakOffsets")
private external fun _nGetBreakOffsets(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetOffsetAtCoord")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetOffsetAtCoord")
private external fun _nGetOffsetAtCoord(ptr: NativePointer, x: Float): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetLeftOffsetAtCoord")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetLeftOffsetAtCoord")
private external fun _nGetLeftOffsetAtCoord(ptr: NativePointer, x: Float): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetCoordAtOffset")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextLine__1nGetCoordAtOffset")
private external fun _nGetCoordAtOffset(ptr: NativePointer, offset: Int): Float
