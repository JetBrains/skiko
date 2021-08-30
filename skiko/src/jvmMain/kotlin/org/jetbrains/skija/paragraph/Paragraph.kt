package org.jetbrains.skija.paragraph

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.skija.impl.Managed.CleanerThunk
import org.jetbrains.skija.paragraph.Shadow
import org.jetbrains.skija.paragraph.TextBox
import org.jetbrains.skija.paragraph.Affinity
import org.jetbrains.skija.paragraph.Paragraph
import org.jetbrains.skija.paragraph.HeightMode
import org.jetbrains.skija.paragraph.StrutStyle
import org.jetbrains.skija.paragraph.BaselineMode
import org.jetbrains.skija.paragraph.RectWidthMode
import org.jetbrains.skija.paragraph.FontCollection
import org.jetbrains.skija.paragraph.ParagraphCache
import org.jetbrains.skija.paragraph.ParagraphStyle
import org.jetbrains.skija.paragraph.RectHeightMode
import org.jetbrains.skija.paragraph.DecorationStyle
import org.jetbrains.skija.paragraph.ParagraphBuilder
import org.jetbrains.skija.paragraph.PlaceholderStyle
import org.jetbrains.skija.paragraph.TextStyleAttribute
import org.jetbrains.skija.paragraph.DecorationLineStyle
import org.jetbrains.skija.paragraph.PlaceholderAlignment
import org.jetbrains.skija.paragraph.PositionWithAffinity
import org.jetbrains.skija.paragraph.TypefaceFontProvider
import org.jetbrains.skija.shaper.Shaper
import org.jetbrains.skija.shaper.FontRun
import org.jetbrains.skija.shaper.LanguageRun
import org.jetbrains.skija.shaper.ShapingOptions
import org.jetbrains.skija.shaper.FontMgrRunIterator
import org.jetbrains.skija.shaper.IcuBidiRunIterator
import org.jetbrains.skija.shaper.ManagedRunIterator
import org.jetbrains.skija.shaper.HbIcuScriptRunIterator
import org.jetbrains.skija.shaper.TextBlobBuilderRunHandler
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import org.jetbrains.skija.skottie.Animation
import org.jetbrains.skija.sksg.InvalidationController
import org.jetbrains.skija.skottie.RenderFlag
import org.jetbrains.skija.skottie.AnimationBuilder
import org.jetbrains.skija.skottie.AnimationBuilderFlag
import org.jetbrains.skija.svg.SVGDOM
import org.jetbrains.skija.svg.SVGSVG
import org.jetbrains.skija.svg.SVGTag
import org.jetbrains.skija.svg.SVGNode
import org.jetbrains.skija.svg.SVGCanvas
import org.jetbrains.skija.svg.SVGLength
import org.jetbrains.skija.svg.SVGLengthType
import org.jetbrains.skija.svg.SVGLengthUnit
import org.jetbrains.skija.svg.SVGLengthContext
import org.jetbrains.skija.svg.SVGPreserveAspectRatio
import org.jetbrains.skija.svg.SVGPreserveAspectRatioAlign
import org.jetbrains.skija.svg.SVGPreserveAspectRatioScale
import org.jetbrains.skija.ColorFilter._LinearToSRGBGammaHolder
import org.jetbrains.skija.ColorFilter._SRGBToLinearGammaHolder
import org.jetbrains.skija.ColorFilter._LumaHolder
import org.jetbrains.skija.ColorSpace._SRGBHolder
import org.jetbrains.skija.ColorSpace._SRGBLinearHolder
import org.jetbrains.skija.ColorSpace._DisplayP3Holder
import org.jetbrains.annotations.ApiStatus.NonExtendable
import org.jetbrains.skija.*
import org.jetbrains.skija.FontMgr._DefaultHolder
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class Paragraph @ApiStatus.Internal constructor(ptr: Long, text: ManagedString?) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        external fun _nGetFinalizer(): Long
        external fun _nGetMaxWidth(ptr: Long): Float
        external fun _nGetHeight(ptr: Long): Float
        external fun _nGetMinIntrinsicWidth(ptr: Long): Float
        external fun _nGetMaxIntrinsicWidth(ptr: Long): Float
        external fun _nGetAlphabeticBaseline(ptr: Long): Float
        external fun _nGetIdeographicBaseline(ptr: Long): Float
        external fun _nGetLongestLine(ptr: Long): Float
        external fun _nDidExceedMaxLines(ptr: Long): Boolean
        external fun _nLayout(ptr: Long, width: Float)
        external fun _nPaint(ptr: Long, canvasPtr: Long, x: Float, y: Float): Long
        external fun _nGetRectsForRange(
            ptr: Long,
            start: Int,
            end: Int,
            rectHeightMode: Int,
            rectWidthMode: Int
        ): Array<TextBox>

        external fun _nGetRectsForPlaceholders(ptr: Long): Array<TextBox>
        external fun _nGetGlyphPositionAtCoordinate(ptr: Long, dx: Float, dy: Float): Int
        external fun _nGetWordBoundary(ptr: Long, offset: Int): Long
        external fun _nGetLineMetrics(ptr: Long, textPtr: Long): Array<LineMetrics?>
        external fun _nGetLineNumber(ptr: Long): Long
        external fun _nMarkDirty(ptr: Long)
        external fun _nGetUnresolvedGlyphsCount(ptr: Long): Int
        external fun _nUpdateAlignment(ptr: Long, Align: Int)

        // public static native void  _nUpdateText(long ptr, int from, String text);
        external fun _nUpdateFontSize(ptr: Long, from: Int, to: Int, size: Float, textPtr: Long)
        external fun _nUpdateForegroundPaint(ptr: Long, from: Int, to: Int, paintPtr: Long, textPtr: Long)
        external fun _nUpdateBackgroundPaint(ptr: Long, from: Int, to: Int, paintPtr: Long, textPtr: Long)

        init {
            staticLoad()
        }
    }

    @ApiStatus.Internal
    var _text: ManagedString?
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
            _nGetRectsForRange(_ptr, start, end, rectHeightMode.ordinal(), rectWidthMode.ordinal())
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

    val lineMetrics: Array<org.jetbrains.skija.paragraph.LineMetrics?>
        get() = try {
            if (_text == null) return arrayOfNulls<LineMetrics>(0)
            Stats.onNativeCall()
            _nGetLineMetrics(_ptr, Native.Companion.getPtr(_text))
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
        _nUpdateAlignment(_ptr, alignment.ordinal())
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

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    init {
        Stats.onNativeCall()
        _text = text
    }
}