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
// No ForceNativeThreadState: returns the address of a cleanup function without calling it, so it is short and cannot
// block.
// Native call path: unrefTextLine
private external fun TextLine_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetWidth")
// No ForceNativeThreadState: returns the stored width, so it is short and cannot block.
// Native call path: TextLine::fWidth
private external fun TextLine_nGetWidth(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetHeight")
// No ForceNativeThreadState: subtracts stored ascent from stored descent and adds stored leading, so it is short and
// cannot block.
// Native call path: TextLine::fAscent
private external fun TextLine_nGetHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetGlyphsLength")
// No ForceNativeThreadState: returns the stored glyph count, so it is short and cannot block.
// Native call path: TextLine::fGlyphCount
private external fun TextLine_nGetGlyphsLength(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetGlyphs")
// Needs ForceNativeThreadState: copies the glyph array of every run in the line, so its time grows with the glyph
// count.
// Native call path: memcpy
@ForceNativeThreadState
private external fun TextLine_nGetGlyphs(ptr: NativePointer, resultGlyphs: InteropPointer, resultLength: Int)

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetPositions")
// Needs ForceNativeThreadState: copies the position array of every run in the line, so its time grows with the glyph
// count.
// Native call path: memcpy
@ForceNativeThreadState
private external fun TextLine_nGetPositions(ptr: NativePointer, resultArray: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetAscent")
// No ForceNativeThreadState: returns the stored ascent, so it is short and cannot block.
// Native call path: TextLine::fAscent
private external fun _nGetAscent(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetCapHeight")
// No ForceNativeThreadState: returns the stored cap height, so it is short and cannot block.
// Native call path: TextLine::fCapHeight
private external fun _nGetCapHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetXHeight")
// No ForceNativeThreadState: returns the stored x-height, so it is short and cannot block.
// Native call path: TextLine::fXHeight
private external fun _nGetXHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetDescent")
// No ForceNativeThreadState: returns the stored descent, so it is short and cannot block.
// Native call path: TextLine::fDescent
private external fun _nGetDescent(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetLeading")
// No ForceNativeThreadState: returns the stored leading, so it is short and cannot block.
// Native call path: TextLine::fLeading
private external fun _nGetLeading(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetTextBlob")
// No ForceNativeThreadState: adds one reference to the stored text blob and returns it, so it is short and cannot
// block.
// Native call path: SkTextBlob::ref
private external fun _nGetTextBlob(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetRunPositions")
// Needs ForceNativeThreadState: copies the position of every run into the result array, so its time grows with the run
// count.
// Native call path: TextLine::fRuns
@ForceNativeThreadState
private external fun _nGetRunPositions(ptr: NativePointer, resultArray: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetRunPositionsCount")
// No ForceNativeThreadState: returns the number of stored runs, so it is short and cannot block.
// Native call path: TextLine::fRuns
private external fun _nGetRunPositionsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetBreakPositionsCount")
// Needs ForceNativeThreadState: loops over every run to add up break position counts, so its time grows with the run
// count.
// Native call path: TextLine::fRuns
@ForceNativeThreadState
private external fun _nGetBreakPositionsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetBreakPositions")
// Needs ForceNativeThreadState: loops over every run and copies its break positions, so its time grows with the text
// length.
// Native call path: memcpy
@ForceNativeThreadState
private external fun _nGetBreakPositions(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetBreakOffsetsCount")
// Needs ForceNativeThreadState: loops over every run to add up break offset counts, so its time grows with the run
// count.
// Native call path: TextLine::fRuns
@ForceNativeThreadState
private external fun _nGetBreakOffsetsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetBreakOffsets")
// Needs ForceNativeThreadState: loops over every run and copies its break offsets, so its time grows with the text
// length.
// Native call path: memcpy
@ForceNativeThreadState
private external fun _nGetBreakOffsets(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetOffsetAtCoord")
// Needs ForceNativeThreadState: scans every run and break position in a nested loop, so its time grows with the text
// length.
// Native call path: TextLine::fRuns
@ForceNativeThreadState
private external fun _nGetOffsetAtCoord(ptr: NativePointer, x: Float): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetLeftOffsetAtCoord")
// Needs ForceNativeThreadState: scans every run and break position in a nested loop, so its time grows with the text
// length.
// Native call path: TextLine::fRuns
@ForceNativeThreadState
private external fun _nGetLeftOffsetAtCoord(ptr: NativePointer, x: Float): Int

@ExternalSymbolName("org_jetbrains_skia_TextLine__1nGetCoordAtOffset")
// Needs ForceNativeThreadState: scans every run and break offset in a nested loop, so its time grows with the text
// length.
// Native call path: TextLine::fRuns
@ForceNativeThreadState
private external fun _nGetCoordAtOffset(ptr: NativePointer, offset: Int): Float
