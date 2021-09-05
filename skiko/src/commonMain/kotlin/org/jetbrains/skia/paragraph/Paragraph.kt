@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import kotlin.jvm.JvmStatic

class Paragraph internal constructor(ptr: Long, text: ManagedString?) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nGetFinalizer")
        external fun _nGetFinalizer(): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nGetMaxWidth")
        external fun _nGetMaxWidth(ptr: Long): Float
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nGetHeight")
        external fun _nGetHeight(ptr: Long): Float
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nGetMinIntrinsicWidth")
        external fun _nGetMinIntrinsicWidth(ptr: Long): Float
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nGetMaxIntrinsicWidth")
        external fun _nGetMaxIntrinsicWidth(ptr: Long): Float
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nGetAlphabeticBaseline")
        external fun _nGetAlphabeticBaseline(ptr: Long): Float
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nGetIdeographicBaseline")
        external fun _nGetIdeographicBaseline(ptr: Long): Float
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nGetLongestLine")
        external fun _nGetLongestLine(ptr: Long): Float
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nDidExceedMaxLines")
        external fun _nDidExceedMaxLines(ptr: Long): Boolean
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nLayout")
        external fun _nLayout(ptr: Long, width: Float)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nPaint")
        external fun _nPaint(ptr: Long, canvasPtr: Long, x: Float, y: Float): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nGetRectsForRange")
        external fun _nGetRectsForRange(
            ptr: Long,
            start: Int,
            end: Int,
            rectHeightMode: Int,
            rectWidthMode: Int
        ): Array<TextBox>

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nGetRectsForPlaceholders")
        external fun _nGetRectsForPlaceholders(ptr: Long): Array<TextBox>
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nGetGlyphPositionAtCoordinate")
        external fun _nGetGlyphPositionAtCoordinate(ptr: Long, dx: Float, dy: Float): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nGetWordBoundary")
        external fun _nGetWordBoundary(ptr: Long, offset: Int): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nGetLineMetrics")
        external fun _nGetLineMetrics(ptr: Long, textPtr: Long): Array<LineMetrics?>
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nGetLineNumber")
        external fun _nGetLineNumber(ptr: Long): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nMarkDirty")
        external fun _nMarkDirty(ptr: Long)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nGetUnresolvedGlyphsCount")
        external fun _nGetUnresolvedGlyphsCount(ptr: Long): Int
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nUpdateAlignment")
        external fun _nUpdateAlignment(ptr: Long, Align: Int)

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nUpdateFontSize")
        external fun _nUpdateFontSize(ptr: Long, from: Int, to: Int, size: Float, textPtr: Long)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nUpdateForegroundPaint")
        external fun _nUpdateForegroundPaint(ptr: Long, from: Int, to: Int, paintPtr: Long, textPtr: Long)
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Paragraph__1nUpdateBackgroundPaint")
        external fun _nUpdateBackgroundPaint(ptr: Long, from: Int, to: Int, paintPtr: Long, textPtr: Long)

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
            _nGetRectsForRange(_ptr, start, end, rectHeightMode.ordinal, rectWidthMode.ordinal)
        } finally {
            reachabilityBarrier(this)
        }
    }

    val rectsForPlaceholders: Array<org.jetbrains.skia.paragraph.TextBox>
        get() = try {
            Stats.onNativeCall()
            _nGetRectsForPlaceholders(_ptr)
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
            IRange.Companion._makeFromLong(_nGetWordBoundary(_ptr, offset))
        } finally {
            reachabilityBarrier(this)
        }
    }

    val lineMetrics: Array<LineMetrics?>
        get() = try {
            if (_text == null) {
                arrayOfNulls(0)
            } else {
                Stats.onNativeCall()
                _nGetLineMetrics(_ptr, getPtr(_text))
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(_text)
        }
    val lineNumber: Long
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
            reachabilityBarrier(paint)
            reachabilityBarrier(_text)
        }
    }

    private object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    init {
        Stats.onNativeCall()
        _text = text
    }
}