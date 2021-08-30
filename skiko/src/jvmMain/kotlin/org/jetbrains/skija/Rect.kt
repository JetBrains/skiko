// Generated by delombok at Mon Aug 30 12:25:17 MSK 2021
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
import org.jetbrains.annotations.Contract
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

open class Rect @ApiStatus.Internal constructor(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float
        get() = right - left
    val height: Float
        get() = bottom - top

    fun intersect(other: Rect): Rect? {
        assert(other != null) { "Rect::intersect expected other != null" }
        return if (right <= other.left || other.right <= left || bottom <= other.top || other.bottom <= top) null else Rect(
            Math.max(
                left, other.left
            ), Math.max(top, other.top), Math.min(
                right, other.right
            ), Math.min(bottom, other.bottom)
        )
    }

    fun scale(scale: Float): Rect {
        return scale(scale, scale)
    }

    fun scale(sx: Float, sy: Float): Rect {
        return Rect(left * sx, top * sy, right * sx, bottom * sy)
    }

    fun offset(dx: Float, dy: Float): Rect {
        return Rect(left + dx, top + dy, right + dx, bottom + dy)
    }

    fun offset(vec: Point): Rect {
        assert(vec != null) { "Rect::offset expected vec != null" }
        return offset(vec._x, vec._y)
    }

    @Contract("-> new")
    fun toIRect(): IRect {
        return IRect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    open fun inflate(spread: Float): Rect {
        return if (spread <= 0) makeLTRB(
            left - spread, top - spread, Math.max(
                left - spread, right + spread
            ), Math.max(top - spread, bottom + spread)
        ) else RRect.Companion.makeLTRB(
            left - spread, top - spread, Math.max(left - spread, right + spread), Math.max(
                top - spread, bottom + spread
            ), spread
        )
    }

    val isEmpty: Boolean
        get() = right == left || top == bottom

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is Rect) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (java.lang.Float.compare(left, other.left) != 0) return false
        if (java.lang.Float.compare(top, other.top) != 0) return false
        if (java.lang.Float.compare(right, other.right) != 0) return false
        return if (java.lang.Float.compare(bottom, other.bottom) != 0) false else true
    }

    protected open fun canEqual(other: Any?): Boolean {
        return other is Rect
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + java.lang.Float.floatToIntBits(left)
        result = result * PRIME + java.lang.Float.floatToIntBits(top)
        result = result * PRIME + java.lang.Float.floatToIntBits(right)
        result = result * PRIME + java.lang.Float.floatToIntBits(bottom)
        return result
    }

    override fun toString(): String {
        return "Rect(_left=" + left + ", _top=" + top + ", _right=" + right + ", _bottom=" + bottom + ")"
    }

    companion object {
        @Contract("_, _, _, _ -> new")
        fun makeLTRB(l: Float, t: Float, r: Float, b: Float): Rect {
            require(l <= r) { "Rect::makeLTRB expected l <= r, got $l > $r" }
            require(t <= b) { "Rect::makeLTRB expected t <= b, got $t > $b" }
            return Rect(l, t, r, b)
        }

        @Contract("_, _ -> new")
        fun makeWH(w: Float, h: Float): Rect {
            require(w >= 0) { "Rect::makeWH expected w >= 0, got: $w" }
            require(h >= 0) { "Rect::makeWH expected h >= 0, got: $h" }
            return Rect(0, 0, w, h)
        }

        @Contract("_, _ -> new")
        fun makeWH(size: Point): Rect {
            assert(size != null) { "Rect::makeWH expected size != null" }
            return makeWH(size._x, size._y)
        }

        @Contract("_, _, _, _ -> new")
        fun makeXYWH(l: Float, t: Float, w: Float, h: Float): Rect {
            require(w >= 0) { "Rect::makeXYWH expected w >= 0, got: $w" }
            require(h >= 0) { "Rect::makeXYWH expected h >= 0, got: $h" }
            return Rect(l, t, l + w, t + h)
        }
    }
}