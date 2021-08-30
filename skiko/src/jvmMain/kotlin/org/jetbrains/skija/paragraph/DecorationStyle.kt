// Generated by delombok at Mon Aug 30 12:25:17 MSK 2021
package org.jetbrains.skija.paragraph

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.skija.impl.Managed.CleanerThunk
import org.jetbrains.skija.paragraph.Shadow
import org.jetbrains.skija.paragraph.TextBox
import org.jetbrains.skija.paragraph.Affinity
import org.jetbrains.skija.ManagedString
import org.jetbrains.skija.paragraph.Paragraph
import org.jetbrains.skija.IRange
import org.jetbrains.skija.FontFeature
import org.jetbrains.skija.Typeface
import org.jetbrains.skija.paragraph.HeightMode
import org.jetbrains.skija.paragraph.StrutStyle
import org.jetbrains.skija.paragraph.BaselineMode
import org.jetbrains.skija.paragraph.RectWidthMode
import org.jetbrains.skija.paragraph.FontCollection
import org.jetbrains.skija.FontMgr
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
import org.jetbrains.skija.TextBlob
import org.jetbrains.skija.shaper.FontRun
import org.jetbrains.skija.FourByteTag
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
import org.jetbrains.skija.Matrix33
import org.jetbrains.skija.svg.SVGDOM
import org.jetbrains.skija.svg.SVGSVG
import org.jetbrains.skija.svg.SVGTag
import org.jetbrains.skija.svg.SVGNode
import org.jetbrains.skija.WStream
import org.jetbrains.skija.svg.SVGCanvas
import org.jetbrains.skija.svg.SVGLength
import org.jetbrains.skija.svg.SVGLengthType
import org.jetbrains.skija.svg.SVGLengthUnit
import org.jetbrains.skija.svg.SVGLengthContext
import org.jetbrains.skija.svg.SVGPreserveAspectRatio
import org.jetbrains.skija.svg.SVGPreserveAspectRatioAlign
import org.jetbrains.skija.svg.SVGPreserveAspectRatioScale
import org.jetbrains.skija.ColorAlphaType
import org.jetbrains.skija.AnimationDisposalMode
import org.jetbrains.skija.BlendMode
import org.jetbrains.skija.IRect
import org.jetbrains.skija.AnimationFrameInfo
import org.jetbrains.skija.BackendRenderTarget
import org.jetbrains.skija.IHasImageInfo
import org.jetbrains.skija.ImageInfo
import org.jetbrains.skija.IPoint
import org.jetbrains.skija.PixelRef
import org.jetbrains.skija.Shader
import org.jetbrains.skija.FilterTileMode
import org.jetbrains.skija.SamplingMode
import org.jetbrains.skija.U16String
import org.jetbrains.skija.SurfaceProps
import org.jetbrains.skija.RRect
import org.jetbrains.skija.ClipMode
import org.jetbrains.skija.FilterMode
import org.jetbrains.skija.Picture
import org.jetbrains.skija.Matrix44
import org.jetbrains.skija.EncodedOrigin
import org.jetbrains.skija.EncodedImageFormat
import org.jetbrains.skija.Color4f
import org.jetbrains.skija.ColorChannel
import org.jetbrains.skija.ColorFilter
import org.jetbrains.skija.ColorMatrix
import org.jetbrains.skija.ColorFilter._LinearToSRGBGammaHolder
import org.jetbrains.skija.ColorFilter._SRGBToLinearGammaHolder
import org.jetbrains.skija.InversionMode
import org.jetbrains.skija.ColorFilter._LumaHolder
import org.jetbrains.skija.ColorInfo
import org.jetbrains.skija.ColorSpace._SRGBHolder
import org.jetbrains.skija.ColorSpace._SRGBLinearHolder
import org.jetbrains.skija.ColorSpace._DisplayP3Holder
import org.jetbrains.skija.ContentChangeMode
import org.jetbrains.skija.CubicResampler
import org.jetbrains.skija.DirectContext
import org.jetbrains.skija.GLBackendState
import org.jetbrains.annotations.ApiStatus.NonExtendable
import org.jetbrains.skija.FilterBlurMode
import org.jetbrains.skija.MipmapMode
import org.jetbrains.skija.FilterMipmap
import org.jetbrains.skija.FilterQuality
import org.jetbrains.skija.FontEdging
import org.jetbrains.skija.FontHinting
import org.jetbrains.skija.FontExtents
import org.jetbrains.skija.FontFamilyName
import org.jetbrains.skija.FontMgr._DefaultHolder
import org.jetbrains.skija.FontStyleSet
import org.jetbrains.skija.FontSlant
import org.jetbrains.skija.FontWidth
import org.jetbrains.skija.FontVariation
import org.jetbrains.skija.FontVariationAxis
import org.jetbrains.skija.GradientStyle
import org.jetbrains.skija.MaskFilter
import org.jetbrains.skija.OutputWStream
import org.jetbrains.skija.PaintMode
import org.jetbrains.skija.PaintStrokeCap
import org.jetbrains.skija.PaintStrokeJoin
import org.jetbrains.skija.PathEffect
import org.jetbrains.skija.PaintFilterCanvas
import org.jetbrains.skija.PathSegment
import org.jetbrains.skija.PathOp
import org.jetbrains.skija.PathFillMode
import org.jetbrains.skija.PathVerb
import org.jetbrains.skija.PathEllipseArc
import org.jetbrains.skija.PathDirection
import org.jetbrains.skija.PathSegmentIterator
import org.jetbrains.skija.RSXform
import org.jetbrains.skija.PathMeasure
import org.jetbrains.skija.PictureRecorder
import org.jetbrains.skija.PixelGeometry
import org.jetbrains.skija.Point3
import org.jetbrains.skija.RuntimeEffect
import org.jetbrains.skija.ShadowUtils
import org.jetbrains.skija.SurfaceOrigin
import org.jetbrains.skija.SurfaceColorFormat
import org.jetbrains.skija.TextBlobBuilder

class DecorationStyle(
    val _underline: Boolean,
    val _overline: Boolean,
    val _lineThrough: Boolean,
    val _gaps: Boolean,
    val color: Int,
    lineStyle: DecorationLineStyle,
    thicknessMultiplier: Float
) {
    val _lineStyle: DecorationLineStyle
    val thicknessMultiplier: Float

    @ApiStatus.Internal
    constructor(
        underline: Boolean,
        overline: Boolean,
        lineThrough: Boolean,
        gaps: Boolean,
        color: Int,
        lineStyle: Int,
        thicknessMultiplier: Float
    ) : this(
        underline,
        overline,
        lineThrough,
        gaps,
        color,
        DecorationLineStyle._values.get(lineStyle),
        thicknessMultiplier
    ) {
    }

    fun hasUnderline(): Boolean {
        return _underline
    }

    fun hasOverline(): Boolean {
        return _overline
    }

    fun hasLineThrough(): Boolean {
        return _lineThrough
    }

    fun hasGaps(): Boolean {
        return _gaps
    }

    val lineStyle: org.jetbrains.skija.paragraph.DecorationLineStyle
        get() = _lineStyle

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is DecorationStyle) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (_underline != other._underline) return false
        if (_overline != other._overline) return false
        if (_lineThrough != other._lineThrough) return false
        if (_gaps != other._gaps) return false
        if (color != other.color) return false
        if (java.lang.Float.compare(thicknessMultiplier, other.thicknessMultiplier) != 0) return false
        val `this$_lineStyle`: Any = lineStyle
        val `other$_lineStyle`: Any = other.lineStyle
        return if (if (`this$_lineStyle` == null) `other$_lineStyle` != null else `this$_lineStyle` != `other$_lineStyle`) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is DecorationStyle
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + if (_underline) 79 else 97
        result = result * PRIME + if (_overline) 79 else 97
        result = result * PRIME + if (_lineThrough) 79 else 97
        result = result * PRIME + if (_gaps) 79 else 97
        result = result * PRIME + color
        result = result * PRIME + java.lang.Float.floatToIntBits(thicknessMultiplier)
        val `$_lineStyle`: Any = lineStyle
        result = result * PRIME + (`$_lineStyle`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "DecorationStyle(_underline=" + _underline + ", _overline=" + _overline + ", _lineThrough=" + _lineThrough + ", _gaps=" + _gaps + ", _color=" + color + ", _lineStyle=" + lineStyle + ", _thicknessMultiplier=" + thicknessMultiplier + ")"
    }

    fun withUnderline(_underline: Boolean): DecorationStyle {
        return if (this._underline == _underline) this else DecorationStyle(
            _underline,
            _overline,
            _lineThrough,
            _gaps,
            color,
            _lineStyle,
            thicknessMultiplier
        )
    }

    fun withOverline(_overline: Boolean): DecorationStyle {
        return if (this._overline == _overline) this else DecorationStyle(
            _underline,
            _overline,
            _lineThrough,
            _gaps,
            color,
            _lineStyle,
            thicknessMultiplier
        )
    }

    fun withLineThrough(_lineThrough: Boolean): DecorationStyle {
        return if (this._lineThrough == _lineThrough) this else DecorationStyle(
            _underline,
            _overline,
            _lineThrough,
            _gaps,
            color,
            _lineStyle,
            thicknessMultiplier
        )
    }

    fun withGaps(_gaps: Boolean): DecorationStyle {
        return if (this._gaps == _gaps) this else DecorationStyle(
            _underline,
            _overline,
            _lineThrough,
            _gaps,
            color,
            _lineStyle,
            thicknessMultiplier
        )
    }

    fun withColor(_color: Int): DecorationStyle {
        return if (color == _color) this else DecorationStyle(
            _underline,
            _overline,
            _lineThrough,
            _gaps,
            _color,
            _lineStyle,
            thicknessMultiplier
        )
    }

    fun withLineStyle(_lineStyle: DecorationLineStyle): DecorationStyle {
        return if (this._lineStyle === _lineStyle) this else DecorationStyle(
            _underline,
            _overline,
            _lineThrough,
            _gaps,
            color,
            _lineStyle,
            thicknessMultiplier
        )
    }

    fun withThicknessMultiplier(_thicknessMultiplier: Float): DecorationStyle {
        return if (thicknessMultiplier == _thicknessMultiplier) this else DecorationStyle(
            _underline,
            _overline,
            _lineThrough,
            _gaps,
            color,
            _lineStyle,
            _thicknessMultiplier
        )
    }

    companion object {
        val NONE: DecorationStyle =
            DecorationStyle(false, false, false, true, -16777216, DecorationLineStyle.SOLID, 1.0f)
    }

    init {
        _lineStyle = lineStyle
        this.thicknessMultiplier = thicknessMultiplier
    }
}