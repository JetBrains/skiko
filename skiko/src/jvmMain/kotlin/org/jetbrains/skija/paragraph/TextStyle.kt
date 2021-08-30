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

class TextStyle @ApiStatus.Internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        external fun _nGetFinalizer(): Long
        external fun _nMake(): Long
        external fun _nEquals(ptr: Long, otherPtr: Long): Boolean
        external fun _nAttributeEquals(ptr: Long, attribute: Int, otherPtr: Long): Boolean
        external fun _nGetColor(ptr: Long): Int
        external fun _nSetColor(ptr: Long, color: Int)
        external fun _nGetForeground(ptr: Long): Long
        external fun _nSetForeground(ptr: Long, paintPtr: Long)
        external fun _nGetBackground(ptr: Long): Long
        external fun _nSetBackground(ptr: Long, paintPtr: Long)
        external fun _nGetDecorationStyle(ptr: Long): DecorationStyle
        external fun _nSetDecorationStyle(
            ptr: Long,
            underline: Boolean,
            overline: Boolean,
            lineThrough: Boolean,
            gaps: Boolean,
            color: Int,
            style: Int,
            thicknessMultiplier: Float
        )

        external fun _nGetFontStyle(ptr: Long): Int
        external fun _nSetFontStyle(ptr: Long, fontStyle: Int)
        external fun _nGetShadows(ptr: Long): Array<Shadow>
        external fun _nAddShadow(ptr: Long, color: Int, offsetX: Float, offsetY: Float, blurSigma: Double)
        external fun _nClearShadows(ptr: Long)
        external fun _nGetFontFeatures(ptr: Long): Array<FontFeature>
        external fun _nAddFontFeature(ptr: Long, name: String?, value: Int)
        external fun _nClearFontFeatures(ptr: Long)
        external fun _nGetFontSize(ptr: Long): Float
        external fun _nSetFontSize(ptr: Long, size: Float)
        external fun _nGetFontFamilies(ptr: Long): Array<String>
        external fun _nSetFontFamilies(ptr: Long, families: Array<String>?)
        external fun _nGetHeight(ptr: Long): Float?
        external fun _nSetHeight(ptr: Long, override: Boolean, height: Float)
        external fun _nGetLetterSpacing(ptr: Long): Float
        external fun _nSetLetterSpacing(ptr: Long, letterSpacing: Float)
        external fun _nGetWordSpacing(ptr: Long): Float
        external fun _nSetWordSpacing(ptr: Long, wordSpacing: Float)
        external fun _nGetTypeface(ptr: Long): Long
        external fun _nSetTypeface(ptr: Long, typefacePtr: Long)
        external fun _nGetLocale(ptr: Long): String
        external fun _nSetLocale(ptr: Long, locale: String?)
        external fun _nGetBaselineMode(ptr: Long): Int
        external fun _nSetBaselineMode(ptr: Long, mode: Int)
        external fun _nGetFontMetrics(ptr: Long): FontMetrics
        external fun _nIsPlaceholder(ptr: Long): Boolean
        external fun _nSetPlaceholder(ptr: Long)

        init {
            staticLoad()
        }
    }

    constructor() : this(_nMake()) {
        Stats.onNativeCall()
    }

    @ApiStatus.Internal
    override fun _nativeEquals(other: Native?): Boolean {
        return try {
            Stats.onNativeCall()
            _nEquals(
                _ptr,
                Native.Companion.getPtr(other)
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(other)
        }
    }

    fun equals(attribute: TextStyleAttribute, other: TextStyle?): Boolean {
        return try {
            Stats.onNativeCall()
            _nAttributeEquals(
                _ptr,
                attribute.ordinal(),
                Native.Companion.getPtr(other)
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(other)
        }
    }

    val color: Int
        get() = try {
            Stats.onNativeCall()
            _nGetColor(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setColor(color: Int): TextStyle {
        Stats.onNativeCall()
        _nSetColor(_ptr, color)
        return this
    }

    val foreground: Paint?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetForeground(_ptr)
            if (ptr == 0L) null else org.jetbrains.skija.Paint(ptr, true)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setForeground(paint: Paint?): TextStyle {
        return try {
            Stats.onNativeCall()
            _nSetForeground(
                _ptr,
                Native.Companion.getPtr(paint)
            )
            this
        } finally {
            Reference.reachabilityFence(paint)
        }
    }

    val background: Paint?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetBackground(_ptr)
            if (ptr == 0L) null else org.jetbrains.skija.Paint(ptr, true)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setBackground(paint: Paint?): TextStyle {
        return try {
            Stats.onNativeCall()
            _nSetBackground(
                _ptr,
                Native.Companion.getPtr(paint)
            )
            this
        } finally {
            Reference.reachabilityFence(paint)
        }
    }

    val decorationStyle: org.jetbrains.skija.paragraph.DecorationStyle
        get() = try {
            Stats.onNativeCall()
            _nGetDecorationStyle(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setDecorationStyle(d: DecorationStyle): TextStyle {
        Stats.onNativeCall()
        _nSetDecorationStyle(
            _ptr,
            d._underline,
            d._overline,
            d._lineThrough,
            d._gaps,
            d._color,
            d._lineStyle.ordinal(),
            d._thicknessMultiplier
        )
        return this
    }

    val fontStyle: FontStyle
        get() = try {
            Stats.onNativeCall()
            org.jetbrains.skija.FontStyle(_nGetFontStyle(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setFontStyle(s: FontStyle): TextStyle {
        Stats.onNativeCall()
        _nSetFontStyle(_ptr, s._value)
        return this
    }

    val shadows: Array<org.jetbrains.skija.paragraph.Shadow>
        get() = try {
            Stats.onNativeCall()
            _nGetShadows(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun addShadow(s: Shadow): TextStyle {
        Stats.onNativeCall()
        _nAddShadow(_ptr, s._color, s._offsetX, s._offsetY, s._blurSigma)
        return this
    }

    fun addShadows(shadows: Array<Shadow>): TextStyle {
        for (s in shadows) addShadow(s)
        return this
    }

    fun clearShadows(): TextStyle {
        Stats.onNativeCall()
        _nClearShadows(_ptr)
        return this
    }

    val fontFeatures: Array<FontFeature>
        get() = try {
            Stats.onNativeCall()
            _nGetFontFeatures(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun addFontFeature(f: FontFeature): TextStyle {
        Stats.onNativeCall()
        _nAddFontFeature(_ptr, f.tag, f._value)
        return this
    }

    fun addFontFeatures(FontFeatures: Array<FontFeature>): TextStyle {
        for (s in FontFeatures) addFontFeature(s)
        return this
    }

    fun clearFontFeatures(): TextStyle {
        Stats.onNativeCall()
        _nClearFontFeatures(_ptr)
        return this
    }

    val fontSize: Float
        get() = try {
            Stats.onNativeCall()
            _nGetFontSize(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setFontSize(size: Float): TextStyle {
        Stats.onNativeCall()
        _nSetFontSize(_ptr, size)
        return this
    }

    val fontFamilies: Array<String>
        get() = try {
            Stats.onNativeCall()
            _nGetFontFamilies(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setFontFamily(family: String): TextStyle {
        return setFontFamilies(arrayOf(family))
    }

    fun setFontFamilies(families: Array<String>?): TextStyle {
        Stats.onNativeCall()
        _nSetFontFamilies(_ptr, families)
        return this
    }

    val height: Float?
        get() = try {
            Stats.onNativeCall()
            _nGetHeight(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setHeight(height: Float?): TextStyle {
        Stats.onNativeCall()
        if (height == null) _nSetHeight(_ptr, false, 0f) else _nSetHeight(_ptr, true, height)
        return this
    }

    val letterSpacing: Float
        get() = try {
            Stats.onNativeCall()
            _nGetLetterSpacing(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setLetterSpacing(letterSpacing: Float): TextStyle {
        Stats.onNativeCall()
        _nSetLetterSpacing(_ptr, letterSpacing)
        return this
    }

    val wordSpacing: Float
        get() = try {
            Stats.onNativeCall()
            _nGetWordSpacing(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setWordSpacing(wordSpacing: Float): TextStyle {
        Stats.onNativeCall()
        _nSetWordSpacing(_ptr, wordSpacing)
        return this
    }

    val typeface: Typeface?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetTypeface(_ptr)
            if (ptr == 0L) null else Typeface(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setTypeface(typeface: Typeface?): TextStyle {
        return try {
            Stats.onNativeCall()
            _nSetTypeface(
                _ptr,
                Native.Companion.getPtr(typeface)
            )
            this
        } finally {
            Reference.reachabilityFence(typeface)
        }
    }

    val locale: String
        get() = try {
            Stats.onNativeCall()
            _nGetLocale(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setLocale(locale: String?): TextStyle {
        Stats.onNativeCall()
        _nSetLocale(_ptr, locale)
        return this
    }

    val baselineMode: org.jetbrains.skija.paragraph.BaselineMode
        get() = try {
            Stats.onNativeCall()
            BaselineMode._values.get(_nGetBaselineMode(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setBaselineMode(baseline: BaselineMode): TextStyle {
        Stats.onNativeCall()
        _nSetBaselineMode(_ptr, baseline.ordinal())
        return this
    }

    val fontMetrics: FontMetrics
        get() = try {
            Stats.onNativeCall()
            _nGetFontMetrics(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val isPlaceholder: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsPlaceholder(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setPlaceholder(): TextStyle {
        Stats.onNativeCall()
        _nSetPlaceholder(_ptr)
        return this
    }

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}