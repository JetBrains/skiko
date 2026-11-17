package org.jetbrains.skia.paragraph

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class Paragraph internal constructor(ptr: NativePointer, text: ManagedString?) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        init {
            staticLoad()
        }
    }

    private var _text: ManagedString?
    override fun close() {
        if (_text != null) {
            _text!!.close()
            _text = null
        }
        super.close()
    }

    val maxWidth: Float
        get() = try {
            Stats.onNativeCall()
            _nGetMaxWidth(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    val height: Float
        get() = try {
            Stats.onNativeCall()
            _nGetHeight(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    val minIntrinsicWidth: Float
        get() = try {
            Stats.onNativeCall()
            _nGetMinIntrinsicWidth(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    val maxIntrinsicWidth: Float
        get() = try {
            Stats.onNativeCall()
            _nGetMaxIntrinsicWidth(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    val alphabeticBaseline: Float
        get() = try {
            Stats.onNativeCall()
            _nGetAlphabeticBaseline(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    val ideographicBaseline: Float
        get() = try {
            Stats.onNativeCall()
            _nGetIdeographicBaseline(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    val longestLine: Float
        get() = try {
            Stats.onNativeCall()
            _nGetLongestLine(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun didExceedMaxLines(): Boolean {
        return try {
            Stats.onNativeCall()
            _nDidExceedMaxLines(_ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun layout(width: Float): Paragraph {
        Stats.onNativeCall()
        _nLayout(_ptr, width)
        return this
    }

    fun paint(canvas: Canvas?, x: Float, y: Float): Paragraph {
        return try {
            Stats.onNativeCall()
            _nPaint(_ptr, getPtr(canvas), x, y)
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(canvas)
        }
    }

    /**
     * Returns a vector of bounding boxes that enclose all text between
     * start and end char indices, including start and excluding end.
     */
    fun getRectsForRange(
        start: Int,
        end: Int,
        rectHeightMode: RectHeightMode,
        rectWidthMode: RectWidthMode
    ): Array<TextBox> {
        return try {
            Stats.onNativeCall()
            interopScope {
                _nGetRectsForRange(_ptr, start, end, rectHeightMode.ordinal, rectWidthMode.ordinal).fromInterop(TextBox)
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    val rectsForPlaceholders: Array<TextBox>
        get() = try {
            Stats.onNativeCall()
            interopScope {
                _nGetRectsForPlaceholders(_ptr).fromInterop(TextBox)
            }
        } finally {
            reachabilityBarrier(this)
        }

    fun getGlyphPositionAtCoordinate(dx: Float, dy: Float): PositionWithAffinity {
        return try {
            Stats.onNativeCall()
            val res = _nGetGlyphPositionAtCoordinate(_ptr, dx, dy)
            if (res >= 0) PositionWithAffinity(res, Affinity.DOWNSTREAM) else PositionWithAffinity(
                -res - 1,
                Affinity.UPSTREAM
            )
        } finally {
            reachabilityBarrier(this)
        }
    }



    fun getWordBoundary(offset: Int): IRange {
        return try {
            Stats.onNativeCall()
            IRange.fromInteropPointer { _nGetWordBoundary(_ptr, offset, it) }
        } finally {
            reachabilityBarrier(this)
        }
    }

    private fun toIRange(p: Long) = IRange((p ushr 32).toInt(), (p and -1).toInt())

    val lineMetrics: Array<LineMetrics>
        get() = try {
            if (_text == null) {
                arrayOf<LineMetrics>()
            } else {
                Stats.onNativeCall()
                interopScope {
                    _nGetLineMetrics(_ptr, getPtr(_text)).fromInterop(LineMetrics)
                }
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(_text)
        }
    val lineNumber: Int
        get() = try {
            Stats.onNativeCall()
            _nGetLineNumber(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun markDirty(): Paragraph {
        Stats.onNativeCall()
        _nMarkDirty(_ptr)
        return this
    }

    val unresolvedGlyphsCount: Int
        get() = try {
            Stats.onNativeCall()
            _nGetUnresolvedGlyphsCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    val unresolvedCodepointsCount: Int
        get() = try {
            Stats.onNativeCall()
            _nGetUnresolvedCodepointsCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    val unresolvedCodepoints: IntArray
        get() = try {
            Stats.onNativeCall()
            val count = _nGetUnresolvedCodepointsCount(_ptr)
            val result = IntArray(count)
            if (count > 0) {
                withResult(result) { _nGetUnresolvedCodepoints(_ptr, it) }
            }
            result
        } finally {
            reachabilityBarrier(this)
        }

    fun updateAlignment(alignment: Alignment): Paragraph {
        Stats.onNativeCall()
        _nUpdateAlignment(_ptr, alignment.ordinal)
        return this
    }

    // public Paragraph updateText(int from, String text) {
    //     Stats.onNativeCall();
    //     _nUpdateText(_ptr, from, text);
    //     // TODO: update _text
    //     return this;
    // }

    /**
     * `from` and `to` are ignored by skia
     * and change applies to the whole paragraph text
     * see: https://github.com/JetBrains/skia/blob/51072f3e6d263eeffed4c3038655ab1bf9cf8439/modules/skparagraph/src/ParagraphImpl.cpp#L1069
     */
    fun updateFontSize(from: Int, to: Int, size: Float): Paragraph {
        return try {
            if (_text != null) {
                Stats.onNativeCall()
                _nUpdateFontSize(
                    _ptr,
                    from,
                    to,
                    size,
                    getPtr(_text)
                )
            }
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(_text)
        }
    }

    /**
     * `from` and `to` are ignored by skia
     * and change applies to the whole paragraph text
     */
    fun updateForegroundPaint(from: Int, to: Int, paint: Paint): Paragraph {
        return try {
            if (_text != null) {
                Stats.onNativeCall()
                _nUpdateForegroundPaint(
                    _ptr,
                    from,
                    to,
                    getPtr(paint),
                    getPtr(_text)
                )
            }
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
            reachabilityBarrier(_text)
        }
    }

    /**
     * `from` and `to` are ignored by skia
     * and change applies to the whole paragraph text
     */
    fun updateBackgroundPaint(from: Int, to: Int, paint: Paint): Paragraph {
        return try {
            if (_text != null) {
                Stats.onNativeCall()
                _nUpdateBackgroundPaint(
                    _ptr,
                    from,
                    to,
                    getPtr(paint),
                    getPtr(_text)
                )
            }
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(paint)
            reachabilityBarrier(_text)
        }
    }

    fun getText(): String {
        return try {
            return _text.toString()
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(_text)
        }
    }

    private object _FinalizerHolder {
        val PTR = Paragraph_nGetFinalizer()
    }

    init {
        Stats.onNativeCall()
        _text = text
    }
}


@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetFinalizer")
// No ForceNativeThreadState: returns the address of a cleanup function without calling it, so it is short and cannot
// block.
// Native call path: deleteParagraph
private external fun Paragraph_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetMaxWidth")
// No ForceNativeThreadState: returns the stored maximum width, so it is short and cannot block.
// Native call path: Paragraph::getMaxWidth
private external fun _nGetMaxWidth(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetHeight")
// No ForceNativeThreadState: returns the stored height, so it is short and cannot block.
// Native call path: Paragraph::getHeight
private external fun _nGetHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetMinIntrinsicWidth")
// No ForceNativeThreadState: returns the stored minimum intrinsic width, so it is short and cannot block.
// Native call path: Paragraph::getMinIntrinsicWidth
private external fun _nGetMinIntrinsicWidth(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetMaxIntrinsicWidth")
// No ForceNativeThreadState: returns the stored maximum intrinsic width, so it is short and cannot block.
// Native call path: Paragraph::getMaxIntrinsicWidth
private external fun _nGetMaxIntrinsicWidth(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetAlphabeticBaseline")
// No ForceNativeThreadState: returns the stored alphabetic baseline, so it is short and cannot block.
// Native call path: Paragraph::getAlphabeticBaseline
private external fun _nGetAlphabeticBaseline(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetIdeographicBaseline")
// No ForceNativeThreadState: returns the stored ideographic baseline, so it is short and cannot block.
// Native call path: Paragraph::getIdeographicBaseline
private external fun _nGetIdeographicBaseline(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetLongestLine")
// No ForceNativeThreadState: returns the stored longest line width, so it is short and cannot block.
// Native call path: Paragraph::getLongestLine
private external fun _nGetLongestLine(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nDidExceedMaxLines")
// No ForceNativeThreadState: returns a stored true or false flag saying whether the text exceeded the maximum line
// count, so it is short and cannot block.
// Native call path: Paragraph::didExceedMaxLines
private external fun _nDidExceedMaxLines(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nLayout")
// Needs ForceNativeThreadState: lays out the text and can look up fallback fonts through CoreText, and it also takes a
// cache lock.
// Native call path: ParagraphImpl::layout -> OneLineShaper -> FontCollection::defaultFallback -> SkFontMgr_Mac
@ForceNativeThreadState
private external fun _nLayout(ptr: NativePointer, width: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nPaint")
// Needs ForceNativeThreadState: draws text using a glyph cache on a canvas that may use the GPU.
// Native call path: ParagraphImpl::paint -> TextLine::paint -> CanvasParagraphPainter -> SkCanvas::drawTextBlob
@ForceNativeThreadState
private external fun _nPaint(ptr: NativePointer, canvasPtr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForRange")
// Needs ForceNativeThreadState: its time grows with the length of the text, which it scans to map offsets, then with
// the number of lines.
// Native call path: ParagraphImpl::getRectsForRange -> ParagraphImpl::ensureUTF16Mapping -> TextLine::getRectsForRange
@ForceNativeThreadState
private external fun _nGetRectsForRange(
    ptr: NativePointer,
    start: Int,
    end: Int,
    rectHeightMode: Int,
    rectWidthMode: Int
): InteropPointer


@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForPlaceholders")
// Needs ForceNativeThreadState: its time grows with the number of lines, since it loops over every line and copies the
// boxes.
// Native call path: ParagraphImpl::getRectsForPlaceholders -> TextLine::getRectsForPlaceholders
@ForceNativeThreadState
private external fun _nGetRectsForPlaceholders(ptr: NativePointer): InteropPointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetGlyphPositionAtCoordinate")
// Needs ForceNativeThreadState: its time grows with the length of the text and with the glyph clusters on every line it
// scans.
// Native call path: ParagraphImpl::getGlyphPositionAtCoordinate -> TextLine::getGlyphPositionAtCoordinate
@ForceNativeThreadState
private external fun _nGetGlyphPositionAtCoordinate(ptr: NativePointer, dx: Float, dy: Float): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetWordBoundary")
// Needs ForceNativeThreadState: on the first call it runs ICU word breaking over the whole text, so its time grows with
// the text length.
// Native call path: ParagraphImpl::getWordBoundary -> SkUnicode::getWords
@ForceNativeThreadState
private external fun _nGetWordBoundary(ptr: NativePointer, offset: Int, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetLineMetrics")
// Needs ForceNativeThreadState: reads font measurements through a cache protected by a lock; a cache miss creates a Mac
// font scaler.
// Native call path: ParagraphImpl::getLineMetrics -> TextLine::getMetrics -> SkFont::getMetrics -> findOrCreateStrike
@ForceNativeThreadState
private external fun _nGetLineMetrics(ptr: NativePointer, textPtr: NativePointer): InteropPointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetLineNumber")
// No ForceNativeThreadState: returns the stored number of lines, so it is short and cannot block.
// Native call path: ParagraphImpl::lineNumber
private external fun _nGetLineNumber(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nMarkDirty")
// No ForceNativeThreadState: marks the paragraph as needing relayout by storing a state value, so it is short and
// cannot block.
// Native call path: ParagraphImpl::markDirty
private external fun _nMarkDirty(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetUnresolvedGlyphsCount")
// No ForceNativeThreadState: checks the paragraph state and returns a stored glyph count, so it is short and cannot
// block.
// Native call path: ParagraphImpl::unresolvedGlyphs
private external fun _nGetUnresolvedGlyphsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetUnresolvedCodepointsCount")
// Needs ForceNativeThreadState: copies the whole set of unresolved codepoints just to count them, so its time grows
// with the number of distinct codepoints.
// Native call path: ParagraphImpl::unresolvedCodepoints
@ForceNativeThreadState
private external fun _nGetUnresolvedCodepointsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetUnresolvedCodepoints")
// Needs ForceNativeThreadState: copies the set of unresolved codepoints and then loops over all of them, so its time
// grows with their count.
// Native call path: ParagraphImpl::unresolvedCodepoints
@ForceNativeThreadState
private external fun _nGetUnresolvedCodepoints(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nUpdateAlignment")
// No ForceNativeThreadState: stores an alignment value in the paragraph style and marks the paragraph state, so it is
// short and cannot block.
// Native call path: ParagraphImpl::updateTextAlign -> ParagraphStyle::setTextAlign
private external fun _nUpdateAlignment(ptr: NativePointer, Align: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nUpdateFontSize")
// Needs ForceNativeThreadState: converts text indices over the whole text, and loops over all text styles, so its time
// grows with the text length and style count.
// Native call path: UtfIndicesConverter::from16To8 -> ParagraphImpl::updateFontSize
@ForceNativeThreadState
private external fun _nUpdateFontSize(ptr: NativePointer, from: Int, to: Int, size: Float, textPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nUpdateForegroundPaint")
// Needs ForceNativeThreadState: replacing each style's paint can free the last reference to a shader or image, and its
// time grows with the number of text styles.
// Native call path: ParagraphImpl::updateForegroundPaint -> TextStyle::setForegroundPaint
@ForceNativeThreadState
private external fun _nUpdateForegroundPaint(ptr: NativePointer, from: Int, to: Int, paintPtr: NativePointer, textPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nUpdateBackgroundPaint")
// Needs ForceNativeThreadState: replacing each style's paint can free the last reference to a shader or image, and its
// time grows with the number of text styles.
// Native call path: ParagraphImpl::updateBackgroundPaint -> TextStyle::setBackgroundPaint
@ForceNativeThreadState
private external fun _nUpdateBackgroundPaint(ptr: NativePointer, from: Int, to: Int, paintPtr: NativePointer, textPtr: NativePointer)
