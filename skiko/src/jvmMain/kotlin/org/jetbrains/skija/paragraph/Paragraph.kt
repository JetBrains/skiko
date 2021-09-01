package org.jetbrains.skija.paragraph

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.skija.*
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class Paragraph internal constructor(ptr: Long, text: ManagedString?) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nGetMaxWidth(ptr: Long): Float
        @JvmStatic external fun _nGetHeight(ptr: Long): Float
        @JvmStatic external fun _nGetMinIntrinsicWidth(ptr: Long): Float
        @JvmStatic external fun _nGetMaxIntrinsicWidth(ptr: Long): Float
        @JvmStatic external fun _nGetAlphabeticBaseline(ptr: Long): Float
        @JvmStatic external fun _nGetIdeographicBaseline(ptr: Long): Float
        @JvmStatic external fun _nGetLongestLine(ptr: Long): Float
        @JvmStatic external fun _nDidExceedMaxLines(ptr: Long): Boolean
        @JvmStatic external fun _nLayout(ptr: Long, width: Float)
        @JvmStatic external fun _nPaint(ptr: Long, canvasPtr: Long, x: Float, y: Float): Long
        @JvmStatic external fun _nGetRectsForRange(
            ptr: Long,
            start: Int,
            end: Int,
            rectHeightMode: Int,
            rectWidthMode: Int
        ): Array<TextBox>

        @JvmStatic external fun _nGetRectsForPlaceholders(ptr: Long): Array<TextBox>
        @JvmStatic external fun _nGetGlyphPositionAtCoordinate(ptr: Long, dx: Float, dy: Float): Int
        @JvmStatic external fun _nGetWordBoundary(ptr: Long, offset: Int): Long
        @JvmStatic external fun _nGetLineMetrics(ptr: Long, textPtr: Long): Array<LineMetrics?>
        @JvmStatic external fun _nGetLineNumber(ptr: Long): Long
        @JvmStatic external fun _nMarkDirty(ptr: Long)
        @JvmStatic external fun _nGetUnresolvedGlyphsCount(ptr: Long): Int
        @JvmStatic external fun _nUpdateAlignment(ptr: Long, Align: Int)

        @JvmStatic external fun _nUpdateFontSize(ptr: Long, from: Int, to: Int, size: Float, textPtr: Long)
        @JvmStatic external fun _nUpdateForegroundPaint(ptr: Long, from: Int, to: Int, paintPtr: Long, textPtr: Long)
        @JvmStatic external fun _nUpdateBackgroundPaint(ptr: Long, from: Int, to: Int, paintPtr: Long, textPtr: Long)

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
            Reference.reachabilityFence(this)
        }
    val height: Float
        get() = try {
            Stats.onNativeCall()
            _nGetHeight(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val minIntrinsicWidth: Float
        get() = try {
            Stats.onNativeCall()
            _nGetMinIntrinsicWidth(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val maxIntrinsicWidth: Float
        get() = try {
            Stats.onNativeCall()
            _nGetMaxIntrinsicWidth(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val alphabeticBaseline: Float
        get() = try {
            Stats.onNativeCall()
            _nGetAlphabeticBaseline(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val ideographicBaseline: Float
        get() = try {
            Stats.onNativeCall()
            _nGetIdeographicBaseline(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val longestLine: Float
        get() = try {
            Stats.onNativeCall()
            _nGetLongestLine(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun didExceedMaxLines(): Boolean {
        return try {
            Stats.onNativeCall()
            _nDidExceedMaxLines(_ptr)
        } finally {
            Reference.reachabilityFence(this)
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
            _nPaint(_ptr, Native.Companion.getPtr(canvas), x, y)
            this
        } finally {
            Reference.reachabilityFence(canvas)
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
            Reference.reachabilityFence(this)
        }
    }

    val rectsForPlaceholders: Array<org.jetbrains.skija.paragraph.TextBox>
        get() = try {
            Stats.onNativeCall()
            _nGetRectsForPlaceholders(_ptr)
        } finally {
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
        }
    }

    fun getWordBoundary(offset: Int): IRange {
        return try {
            Stats.onNativeCall()
            IRange.Companion._makeFromLong(_nGetWordBoundary(_ptr, offset))
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    val lineMetrics: Array<LineMetrics?>
        get() = try {
            if (_text == null) {
                arrayOfNulls(0)
            } else {
                Stats.onNativeCall()
                _nGetLineMetrics(_ptr, Native.Companion.getPtr(_text))
            }
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(_text)
        }
    val lineNumber: Long
        get() = try {
            Stats.onNativeCall()
            _nGetLineNumber(_ptr)
        } finally {
            Reference.reachabilityFence(this)
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
            Reference.reachabilityFence(this)
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
                    Native.Companion.getPtr(_text)
                )
            }
            this
        } finally {
            Reference.reachabilityFence(_text)
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
                    Native.Companion.getPtr(paint),
                    Native.Companion.getPtr(_text)
                )
            }
            this
        } finally {
            Reference.reachabilityFence(paint)
            Reference.reachabilityFence(_text)
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
                    Native.Companion.getPtr(paint),
                    Native.Companion.getPtr(_text)
                )
            }
            this
        } finally {
            Reference.reachabilityFence(paint)
            Reference.reachabilityFence(_text)
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