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

    fun updateForegroundPaint(from: Int, to: Int, paint: Paint?): Paragraph {
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

    fun updateBackgroundPaint(from: Int, to: Int, paint: Paint?): Paragraph {
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
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetFinalizer")
private external fun Paragraph_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetMaxWidth")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetMaxWidth")
private external fun _nGetMaxWidth(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetHeight")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetHeight")
private external fun _nGetHeight(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetMinIntrinsicWidth")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetMinIntrinsicWidth")
private external fun _nGetMinIntrinsicWidth(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetMaxIntrinsicWidth")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetMaxIntrinsicWidth")
private external fun _nGetMaxIntrinsicWidth(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetAlphabeticBaseline")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetAlphabeticBaseline")
private external fun _nGetAlphabeticBaseline(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetIdeographicBaseline")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetIdeographicBaseline")
private external fun _nGetIdeographicBaseline(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetLongestLine")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetLongestLine")
private external fun _nGetLongestLine(ptr: NativePointer): Float

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nDidExceedMaxLines")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nDidExceedMaxLines")
private external fun _nDidExceedMaxLines(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nLayout")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nLayout")
private external fun _nLayout(ptr: NativePointer, width: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nPaint")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nPaint")
private external fun _nPaint(ptr: NativePointer, canvasPtr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForRange")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForRange")
private external fun _nGetRectsForRange(
    ptr: NativePointer,
    start: Int,
    end: Int,
    rectHeightMode: Int,
    rectWidthMode: Int
): InteropPointer


@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForPlaceholders")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetRectsForPlaceholders")
private external fun _nGetRectsForPlaceholders(ptr: NativePointer): InteropPointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetGlyphPositionAtCoordinate")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetGlyphPositionAtCoordinate")
private external fun _nGetGlyphPositionAtCoordinate(ptr: NativePointer, dx: Float, dy: Float): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetWordBoundary")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetWordBoundary")
private external fun _nGetWordBoundary(ptr: NativePointer, offset: Int, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetLineMetrics")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetLineMetrics")
private external fun _nGetLineMetrics(ptr: NativePointer, textPtr: NativePointer): InteropPointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetLineNumber")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetLineNumber")
private external fun _nGetLineNumber(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nMarkDirty")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nMarkDirty")
private external fun _nMarkDirty(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nGetUnresolvedGlyphsCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nGetUnresolvedGlyphsCount")
private external fun _nGetUnresolvedGlyphsCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nUpdateAlignment")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nUpdateAlignment")
private external fun _nUpdateAlignment(ptr: NativePointer, Align: Int)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nUpdateFontSize")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nUpdateFontSize")
private external fun _nUpdateFontSize(ptr: NativePointer, from: Int, to: Int, size: Float, textPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nUpdateForegroundPaint")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nUpdateForegroundPaint")
private external fun _nUpdateForegroundPaint(ptr: NativePointer, from: Int, to: Int, paintPtr: NativePointer, textPtr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_Paragraph__1nUpdateBackgroundPaint")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_Paragraph__1nUpdateBackgroundPaint")
private external fun _nUpdateBackgroundPaint(ptr: NativePointer, from: Int, to: Int, paintPtr: NativePointer, textPtr: NativePointer)
