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
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class Shader @ApiStatus.Internal constructor(ptr: Long) : RefCnt(ptr) {
    companion object {
        // Linear
        fun makeLinearGradient(p0: Point, p1: Point, colors: IntArray): Shader {
            return makeLinearGradient(p0._x, p0._y, p1._x, p1._y, colors)
        }

        fun makeLinearGradient(p0: Point, p1: Point, colors: IntArray, positions: FloatArray?): Shader {
            return makeLinearGradient(p0._x, p0._y, p1._x, p1._y, colors, positions)
        }

        fun makeLinearGradient(
            p0: Point,
            p1: Point,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeLinearGradient(p0._x, p0._y, p1._x, p1._y, colors, positions, style)
        }

        @JvmOverloads
        fun makeLinearGradient(
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            colors: IntArray,
            positions: FloatArray? = null,
            style: GradientStyle = GradientStyle.Companion.DEFAULT
        ): Shader {
            assert(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
            Stats.onNativeCall()
            return Shader(
                _nMakeLinearGradient(
                    x0,
                    y0,
                    x1,
                    y1,
                    colors,
                    positions,
                    style.tileMode.ordinal,
                    style._getFlags(),
                    style._getMatrixArray()
                )
            )
        }

        fun makeLinearGradient(
            p0: Point,
            p1: Point,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeLinearGradient(p0._x, p0._y, p1._x, p1._y, colors, cs, positions, style)
        }

        fun makeLinearGradient(
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return try {
                assert(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
                Stats.onNativeCall()
                Shader(
                    _nMakeLinearGradientCS(
                        x0,
                        y0,
                        x1,
                        y1,
                        Color4f.Companion.flattenArray(colors),
                        Native.Companion.getPtr(cs),
                        positions,
                        style.tileMode.ordinal,
                        style._getFlags(),
                        style._getMatrixArray()
                    )
                )
            } finally {
                Reference.reachabilityFence(cs)
            }
        }

        // Radial
        fun makeRadialGradient(center: Point, r: Float, colors: IntArray): Shader {
            return makeRadialGradient(center._x, center._y, r, colors)
        }

        fun makeRadialGradient(center: Point, r: Float, colors: IntArray, positions: FloatArray?): Shader {
            return makeRadialGradient(center._x, center._y, r, colors, positions)
        }

        fun makeRadialGradient(
            center: Point,
            r: Float,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeRadialGradient(center._x, center._y, r, colors, positions, style)
        }

        @JvmOverloads
        fun makeRadialGradient(
            x: Float,
            y: Float,
            r: Float,
            colors: IntArray,
            positions: FloatArray? = null,
            style: GradientStyle = GradientStyle.Companion.DEFAULT
        ): Shader {
            assert(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
            Stats.onNativeCall()
            return Shader(
                _nMakeRadialGradient(
                    x,
                    y,
                    r,
                    colors,
                    positions,
                    style.tileMode.ordinal,
                    style._getFlags(),
                    style._getMatrixArray()
                )
            )
        }

        fun makeRadialGradient(
            center: Point,
            r: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeRadialGradient(center._x, center._y, r, colors, cs, positions, style)
        }

        fun makeRadialGradient(
            x: Float,
            y: Float,
            r: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return try {
                assert(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
                Stats.onNativeCall()
                Shader(
                    _nMakeRadialGradientCS(
                        x,
                        y,
                        r,
                        Color4f.Companion.flattenArray(colors),
                        Native.Companion.getPtr(cs),
                        positions,
                        style.tileMode.ordinal,
                        style._getFlags(),
                        style._getMatrixArray()
                    )
                )
            } finally {
                Reference.reachabilityFence(cs)
            }
        }

        // Two-point Conical
        fun makeTwoPointConicalGradient(p0: Point, r0: Float, p1: Point, r1: Float, colors: IntArray): Shader {
            return makeTwoPointConicalGradient(p0._x, p0._y, r0, p1._x, p1._y, r1, colors)
        }

        fun makeTwoPointConicalGradient(
            p0: Point,
            r0: Float,
            p1: Point,
            r1: Float,
            colors: IntArray,
            positions: FloatArray?
        ): Shader {
            return makeTwoPointConicalGradient(p0._x, p0._y, r0, p1._x, p1._y, r1, colors, positions)
        }

        fun makeTwoPointConicalGradient(
            p0: Point,
            r0: Float,
            p1: Point,
            r1: Float,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeTwoPointConicalGradient(p0._x, p0._y, r0, p1._x, p1._y, r1, colors, positions, style)
        }

        @JvmOverloads
        fun makeTwoPointConicalGradient(
            x0: Float,
            y0: Float,
            r0: Float,
            x1: Float,
            y1: Float,
            r1: Float,
            colors: IntArray,
            positions: FloatArray? = null,
            style: GradientStyle = GradientStyle.Companion.DEFAULT
        ): Shader {
            assert(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
            Stats.onNativeCall()
            return Shader(
                _nMakeTwoPointConicalGradient(
                    x0,
                    y0,
                    r0,
                    x1,
                    y1,
                    r1,
                    colors,
                    positions,
                    style.tileMode.ordinal,
                    style._getFlags(),
                    style._getMatrixArray()
                )
            )
        }

        fun makeTwoPointConicalGradient(
            p0: Point,
            r0: Float,
            p1: Point,
            r1: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeTwoPointConicalGradient(p0._x, p0._y, r0, p1._x, p1._y, r1, colors, cs, positions, style)
        }

        fun makeTwoPointConicalGradient(
            x0: Float,
            y0: Float,
            r0: Float,
            x1: Float,
            y1: Float,
            r1: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return try {
                assert(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
                Stats.onNativeCall()
                Shader(
                    _nMakeTwoPointConicalGradientCS(
                        x0,
                        y0,
                        r0,
                        x1,
                        y1,
                        r1,
                        Color4f.Companion.flattenArray(colors),
                        Native.Companion.getPtr(cs),
                        positions,
                        style.tileMode.ordinal,
                        style._getFlags(),
                        style._getMatrixArray()
                    )
                )
            } finally {
                Reference.reachabilityFence(cs)
            }
        }

        // Sweep
        fun makeSweepGradient(center: Point, colors: IntArray): Shader {
            return makeSweepGradient(center._x, center._y, colors)
        }

        fun makeSweepGradient(x: Float, y: Float, colors: IntArray): Shader {
            return makeSweepGradient(x, y, 0f, 360f, colors, null, GradientStyle.Companion.DEFAULT)
        }

        fun makeSweepGradient(center: Point, colors: IntArray, positions: FloatArray?): Shader {
            return makeSweepGradient(center._x, center._y, colors, positions)
        }

        fun makeSweepGradient(x: Float, y: Float, colors: IntArray, positions: FloatArray?): Shader {
            return makeSweepGradient(x, y, 0f, 360f, colors, positions, GradientStyle.Companion.DEFAULT)
        }

        fun makeSweepGradient(center: Point, colors: IntArray, positions: FloatArray?, style: GradientStyle): Shader {
            return makeSweepGradient(center._x, center._y, colors, positions, style)
        }

        fun makeSweepGradient(
            x: Float,
            y: Float,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeSweepGradient(x, y, 0f, 360f, colors, positions, style)
        }

        fun makeSweepGradient(
            center: Point,
            startAngle: Float,
            endAngle: Float,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeSweepGradient(center._x, center._y, startAngle, endAngle, colors, positions, style)
        }

        fun makeSweepGradient(
            x: Float,
            y: Float,
            startAngle: Float,
            endAngle: Float,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            assert(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
            Stats.onNativeCall()
            return Shader(
                _nMakeSweepGradient(
                    x,
                    y,
                    startAngle,
                    endAngle,
                    colors,
                    positions,
                    style.tileMode.ordinal,
                    style._getFlags(),
                    style._getMatrixArray()
                )
            )
        }

        fun makeSweepGradient(
            center: Point,
            startAngle: Float,
            endAngle: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeSweepGradient(center._x, center._y, startAngle, endAngle, colors, cs, positions, style)
        }

        fun makeSweepGradient(
            x: Float,
            y: Float,
            startAngle: Float,
            endAngle: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return try {
                assert(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
                Stats.onNativeCall()
                Shader(
                    _nMakeSweepGradientCS(
                        x,
                        y,
                        startAngle,
                        endAngle,
                        Color4f.Companion.flattenArray(colors),
                        Native.Companion.getPtr(cs),
                        positions,
                        style.tileMode.ordinal,
                        style._getFlags(),
                        style._getMatrixArray()
                    )
                )
            } finally {
                Reference.reachabilityFence(cs)
            }
        }

        //
        fun makeEmpty(): Shader {
            Stats.onNativeCall()
            return Shader(_nMakeEmpty())
        }

        fun makeColor(color: Int): Shader {
            Stats.onNativeCall()
            return Shader(_nMakeColor(color))
        }

        fun makeColor(color: Color4f, space: ColorSpace?): Shader {
            return try {
                Stats.onNativeCall()
                Shader(
                    _nMakeColorCS(
                        color.r,
                        color.g,
                        color.b,
                        color.a,
                        Native.Companion.getPtr(space)
                    )
                )
            } finally {
                Reference.reachabilityFence(space)
            }
        }

        fun makeBlend(mode: BlendMode, dst: Shader?, src: Shader?): Shader {
            return try {
                Stats.onNativeCall()
                Shader(
                    _nMakeBlend(
                        mode.ordinal,
                        Native.Companion.getPtr(dst),
                        Native.Companion.getPtr(src)
                    )
                )
            } finally {
                Reference.reachabilityFence(dst)
                Reference.reachabilityFence(src)
            }
        }

        external fun _nMakeWithColorFilter(ptr: Long, colorFilterPtr: Long): Long
        external fun _nMakeLinearGradient(
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            colors: IntArray?,
            positions: FloatArray?,
            tileType: Int,
            flags: Int,
            matrix: FloatArray?
        ): Long

        external fun _nMakeLinearGradientCS(
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            colors: FloatArray?,
            colorSpacePtr: Long,
            positions: FloatArray?,
            tileType: Int,
            flags: Int,
            matrix: FloatArray?
        ): Long

        external fun _nMakeRadialGradient(
            x: Float,
            y: Float,
            r: Float,
            colors: IntArray?,
            positions: FloatArray?,
            tileType: Int,
            flags: Int,
            matrix: FloatArray?
        ): Long

        external fun _nMakeRadialGradientCS(
            x: Float,
            y: Float,
            r: Float,
            colors: FloatArray?,
            colorSpacePtr: Long,
            positions: FloatArray?,
            tileType: Int,
            flags: Int,
            matrix: FloatArray?
        ): Long

        external fun _nMakeTwoPointConicalGradient(
            x0: Float,
            y0: Float,
            r0: Float,
            x1: Float,
            y1: Float,
            r1: Float,
            colors: IntArray?,
            positions: FloatArray?,
            tileType: Int,
            flags: Int,
            matrix: FloatArray?
        ): Long

        external fun _nMakeTwoPointConicalGradientCS(
            x0: Float,
            y0: Float,
            r0: Float,
            x1: Float,
            y1: Float,
            r1: Float,
            colors: FloatArray?,
            colorSpacePtr: Long,
            positions: FloatArray?,
            tileType: Int,
            flags: Int,
            matrix: FloatArray?
        ): Long

        external fun _nMakeSweepGradient(
            x: Float,
            y: Float,
            startAngle: Float,
            endAngle: Float,
            colors: IntArray?,
            positions: FloatArray?,
            tileType: Int,
            flags: Int,
            matrix: FloatArray?
        ): Long

        external fun _nMakeSweepGradientCS(
            x: Float,
            y: Float,
            startAngle: Float,
            endAngle: Float,
            colors: FloatArray?,
            colorSpacePtr: Long,
            positions: FloatArray?,
            tileType: Int,
            flags: Int,
            matrix: FloatArray?
        ): Long

        external fun _nMakeEmpty(): Long
        external fun _nMakeColor(color: Int): Long
        external fun _nMakeColorCS(r: Float, g: Float, b: Float, a: Float, colorSpacePtr: Long): Long
        external fun _nMakeBlend(blendMode: Int, dst: Long, src: Long): Long

        init {
            staticLoad()
        }
    }

    fun makeWithColorFilter(f: ColorFilter?): Shader {
        return try {
            Shader(_nMakeWithColorFilter(_ptr, Native.Companion.getPtr(f)))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(f)
        }
    }
}