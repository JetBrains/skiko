package org.jetbrains.skija

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

class FontMetrics(
    /**
     * greatest extent above origin of any glyph bounding box, typically negative; deprecated with variable fonts
     */
    val top: Float,
    /**
     * distance to reserve above baseline, typically negative
     */
    val ascent: Float,
    /**
     * distance to reserve below baseline, typically positive
     */
    val descent: Float,
    /**
     * greatest extent below origin of any glyph bounding box, typically positive; deprecated with variable fonts
     */
    val bottom: Float,
    /**
     * distance to add between lines, typically positive or zero
     */
    val leading: Float,
    /**
     * average character width, zero if unknown
     */
    val avgCharWidth: Float,
    /**
     * maximum character width, zero if unknown
     */
    val maxCharWidth: Float,
    /**
     * greatest extent to left of origin of any glyph bounding box, typically negative; deprecated with variable fonts
     */
    val xMin: Float,
    /**
     * greatest extent to right of origin of any glyph bounding box, typically positive; deprecated with variable fonts
     */
    val xMax: Float,
    /**
     * height of lower-case 'x', zero if unknown, typically negative
     */
    val xHeight: Float,
    /**
     * height of an upper-case letter, zero if unknown, typically negative
     */
    val capHeight: Float,
    /**
     * underline thickness
     */
    val underlineThickness: Float?,
    /**
     * distance from baseline to top of stroke, typically positive
     */
    val underlinePosition: Float?,
    /**
     * strikeout thickness
     */
    val strikeoutThickness: Float?,
    /**
     * distance from baseline to bottom of stroke, typically negative
     */
    val strikeoutPosition: Float?
) {
    /**
     * greatest extent above origin of any glyph bounding box, typically negative; deprecated with variable fonts
     */
    /**
     * distance to reserve above baseline, typically negative
     */
    /**
     * distance to reserve below baseline, typically positive
     */
    /**
     * greatest extent below origin of any glyph bounding box, typically positive; deprecated with variable fonts
     */
    /**
     * distance to add between lines, typically positive or zero
     */
    /**
     * average character width, zero if unknown
     */
    /**
     * maximum character width, zero if unknown
     */
    /**
     * greatest extent to left of origin of any glyph bounding box, typically negative; deprecated with variable fonts
     */
    /**
     * greatest extent to right of origin of any glyph bounding box, typically positive; deprecated with variable fonts
     */
    /**
     * height of lower-case 'x', zero if unknown, typically negative
     */
    /**
     * height of an upper-case letter, zero if unknown, typically negative
     */
    /**
     * underline thickness
     */
    /**
     * distance from baseline to top of stroke, typically positive
     */
    /**
     * strikeout thickness
     */
    /**
     * distance from baseline to bottom of stroke, typically negative
     */
    val height: Float
        get() = descent - ascent

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is FontMetrics) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (java.lang.Float.compare(top, other.top) != 0) return false
        if (java.lang.Float.compare(ascent, other.ascent) != 0) return false
        if (java.lang.Float.compare(descent, other.descent) != 0) return false
        if (java.lang.Float.compare(bottom, other.bottom) != 0) return false
        if (java.lang.Float.compare(leading, other.leading) != 0) return false
        if (java.lang.Float.compare(avgCharWidth, other.avgCharWidth) != 0) return false
        if (java.lang.Float.compare(maxCharWidth, other.maxCharWidth) != 0) return false
        if (java.lang.Float.compare(xMin, other.xMin) != 0) return false
        if (java.lang.Float.compare(xMax, other.xMax) != 0) return false
        if (java.lang.Float.compare(xHeight, other.xHeight) != 0) return false
        if (java.lang.Float.compare(capHeight, other.capHeight) != 0) return false
        val `this$_underlineThickness`: Any? = underlineThickness
        val `other$_underlineThickness`: Any? = other.underlineThickness
        if (if (`this$_underlineThickness` == null) `other$_underlineThickness` != null else `this$_underlineThickness` != `other$_underlineThickness`) return false
        val `this$_underlinePosition`: Any? = underlinePosition
        val `other$_underlinePosition`: Any? = other.underlinePosition
        if (if (`this$_underlinePosition` == null) `other$_underlinePosition` != null else `this$_underlinePosition` != `other$_underlinePosition`) return false
        val `this$_strikeoutThickness`: Any? = strikeoutThickness
        val `other$_strikeoutThickness`: Any? = other.strikeoutThickness
        if (if (`this$_strikeoutThickness` == null) `other$_strikeoutThickness` != null else `this$_strikeoutThickness` != `other$_strikeoutThickness`) return false
        val `this$_strikeoutPosition`: Any? = strikeoutPosition
        val `other$_strikeoutPosition`: Any? = other.strikeoutPosition
        return if (if (`this$_strikeoutPosition` == null) `other$_strikeoutPosition` != null else `this$_strikeoutPosition` != `other$_strikeoutPosition`) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is FontMetrics
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + java.lang.Float.floatToIntBits(top)
        result = result * PRIME + java.lang.Float.floatToIntBits(ascent)
        result = result * PRIME + java.lang.Float.floatToIntBits(descent)
        result = result * PRIME + java.lang.Float.floatToIntBits(bottom)
        result = result * PRIME + java.lang.Float.floatToIntBits(leading)
        result = result * PRIME + java.lang.Float.floatToIntBits(avgCharWidth)
        result = result * PRIME + java.lang.Float.floatToIntBits(maxCharWidth)
        result = result * PRIME + java.lang.Float.floatToIntBits(xMin)
        result = result * PRIME + java.lang.Float.floatToIntBits(xMax)
        result = result * PRIME + java.lang.Float.floatToIntBits(xHeight)
        result = result * PRIME + java.lang.Float.floatToIntBits(capHeight)
        val `$_underlineThickness`: Any? = underlineThickness
        result = result * PRIME + (`$_underlineThickness`?.hashCode() ?: 43)
        val `$_underlinePosition`: Any? = underlinePosition
        result = result * PRIME + (`$_underlinePosition`?.hashCode() ?: 43)
        val `$_strikeoutThickness`: Any? = strikeoutThickness
        result = result * PRIME + (`$_strikeoutThickness`?.hashCode() ?: 43)
        val `$_strikeoutPosition`: Any? = strikeoutPosition
        result = result * PRIME + (`$_strikeoutPosition`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "FontMetrics(_top=" + top + ", _ascent=" + ascent + ", _descent=" + descent + ", _bottom=" + bottom + ", _leading=" + leading + ", _avgCharWidth=" + avgCharWidth + ", _maxCharWidth=" + maxCharWidth + ", _xMin=" + xMin + ", _xMax=" + xMax + ", _xHeight=" + xHeight + ", _capHeight=" + capHeight + ", _underlineThickness=" + underlineThickness + ", _underlinePosition=" + underlinePosition + ", _strikeoutThickness=" + strikeoutThickness + ", _strikeoutPosition=" + strikeoutPosition + ")"
    }
}