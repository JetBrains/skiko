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
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class TextBlobBuilder @ApiStatus.Internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @ApiStatus.Internal
        external fun _nGetFinalizer(): Long
        @ApiStatus.Internal
        external fun _nMake(): Long
        @ApiStatus.Internal
        external fun _nBuild(ptr: Long): Long
        @ApiStatus.Internal
        external fun _nAppendRun(ptr: Long, fontPtr: Long, glyphs: ShortArray?, x: Float, y: Float, bounds: Rect?)
        @ApiStatus.Internal
        external fun _nAppendRunPosH(
            ptr: Long,
            fontPtr: Long,
            glyphs: ShortArray?,
            xs: FloatArray?,
            y: Float,
            bounds: Rect?
        )

        @ApiStatus.Internal
        external fun _nAppendRunPos(ptr: Long, fontPtr: Long, glyphs: ShortArray?, pos: FloatArray?, bounds: Rect?)
        @ApiStatus.Internal
        external fun _nAppendRunRSXform(ptr: Long, fontPtr: Long, glyphs: ShortArray?, xform: FloatArray?)

        init {
            staticLoad()
        }
    }

    /**
     * Constructs empty TextBlobBuilder. By default, TextBlobBuilder has no runs.
     *
     * @see [https://fiddle.skia.org/c/@TextBlobBuilder_empty_constructor](https://fiddle.skia.org/c/@TextBlobBuilder_empty_constructor)
     */
    constructor() : this(_nMake()) {
        Stats.onNativeCall()
    }

    /**
     *
     * Returns TextBlob built from runs of glyphs added by builder. Returned
     * TextBlob is immutable; it may be copied, but its contents may not be altered.
     * Returns null if no runs of glyphs were added by builder.
     *
     *
     * Resets TextBlobBuilder to its initial empty state, allowing it to be
     * reused to build a new set of runs.
     *
     * @return  SkTextBlob or null
     *
     * @see [https://fiddle.skia.org/c/@TextBlobBuilder_make](https://fiddle.skia.org/c/@TextBlobBuilder_make)
     */
    fun build(): TextBlob? {
        return try {
            Stats.onNativeCall()
            val ptr = _nBuild(_ptr)
            if (ptr == 0L) null else TextBlob(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    /**
     * Glyphs are positioned on a baseline at (x, y), using font metrics to
     * determine their relative placement.
     *
     * @param font    Font used for this run
     * @param text    Text to append in this run
     * @param x       horizontal offset within the blob
     * @param y       vertical offset within the blob
     * @return        this
     */
    fun appendRun(font: Font, text: String, x: Float, y: Float): TextBlobBuilder {
        return appendRun(font, font.getStringGlyphs(text), x, y, null)
    }

    /**
     *
     * Glyphs are positioned on a baseline at (x, y), using font metrics to
     * determine their relative placement.
     *
     *
     * bounds defines an optional bounding box, used to suppress drawing when TextBlob
     * bounds does not intersect Surface bounds. If bounds is null, TextBlob bounds
     * is computed from (x, y) and glyphs metrics.
     *
     * @param font    Font used for this run
     * @param text    Text to append in this run
     * @param x       horizontal offset within the blob
     * @param y       vertical offset within the blob
     * @param bounds  optional run bounding box
     * @return        this
     */
    fun appendRun(font: Font, text: String, x: Float, y: Float, bounds: Rect?): TextBlobBuilder {
        return appendRun(font, font.getStringGlyphs(text), x, y, bounds)
    }

    /**
     *
     * Glyphs are positioned on a baseline at (x, y), using font metrics to
     * determine their relative placement.
     *
     *
     * bounds defines an optional bounding box, used to suppress drawing when TextBlob
     * bounds does not intersect Surface bounds. If bounds is null, TextBlob bounds
     * is computed from (x, y) and glyphs metrics.
     *
     * @param font    Font used for this run
     * @param glyphs  glyphs in this run
     * @param x       horizontal offset within the blob
     * @param y       vertical offset within the blob
     * @param bounds  optional run bounding box
     * @return        this
     */
    fun appendRun(font: Font?, glyphs: ShortArray?, x: Float, y: Float, bounds: Rect?): TextBlobBuilder {
        return try {
            Stats.onNativeCall()
            _nAppendRun(
                _ptr,
                Native.Companion.getPtr(font),
                glyphs,
                x,
                y,
                bounds
            )
            this
        } finally {
            Reference.reachabilityFence(font)
        }
    }
    /**
     *
     * Glyphs are positioned on a baseline at y, using x-axis positions from xs.
     *
     *
     * bounds defines an optional bounding box, used to suppress drawing when TextBlob
     * bounds does not intersect Surface bounds. If bounds is null, TextBlob bounds
     * is computed from (x, y) and glyphs metrics.
     *
     * @param font    Font used for this run
     * @param glyphs  glyphs in this run
     * @param xs      horizontal positions of glyphs within the blob
     * @param y       vertical offset within the blob
     * @param bounds  optional run bounding box
     * @return        this
     */
    /**
     *
     * Glyphs are positioned on a baseline at y, using x-axis positions from xs.
     *
     * @param font    Font used for this run
     * @param glyphs  glyphs in this run
     * @param xs      horizontal positions on glyphs within the blob
     * @param y       vertical offset within the blob
     * @return        this
     */
    @JvmOverloads
    fun appendRunPosH(
        font: Font?,
        glyphs: ShortArray,
        xs: FloatArray,
        y: Float,
        bounds: Rect? = null
    ): TextBlobBuilder {
        return try {
            assert(glyphs.size == xs.size) { "glyphs.length " + glyphs.size + " != xs.length " + xs.size }
            Stats.onNativeCall()
            _nAppendRunPosH(
                _ptr,
                Native.Companion.getPtr(font),
                glyphs,
                xs,
                y,
                bounds
            )
            this
        } finally {
            Reference.reachabilityFence(font)
        }
    }
    /**
     *
     * Glyphs are positioned at positions from pos.
     *
     *
     * bounds defines an optional bounding box, used to suppress drawing when TextBlob
     * bounds does not intersect Surface bounds. If bounds is null, TextBlob bounds
     * is computed from (x, y) and glyphs metrics.
     *
     * @param font    Font used for this run
     * @param glyphs  glyphs in this run
     * @param pos     positions of glyphs within the blob
     * @param bounds  optional run bounding box
     * @return        this
     */
    /**
     *
     * Glyphs are positioned at positions from pos.
     *
     * @param font    Font used for this run
     * @param glyphs  glyphs in this run
     * @param pos     positions of glyphs within the blob
     * @return        this
     */
    @JvmOverloads
    fun appendRunPos(font: Font?, glyphs: ShortArray, pos: Array<Point>, bounds: Rect? = null): TextBlobBuilder {
        return try {
            assert(glyphs.size == pos.size) { "glyphs.length " + glyphs.size + " != pos.length " + pos.size }
            val floatPos = FloatArray(pos.size * 2)
            for (i in pos.indices) {
                floatPos[i * 2] = pos[i]._x
                floatPos[i * 2 + 1] = pos[i]._y
            }
            Stats.onNativeCall()
            _nAppendRunPos(
                _ptr,
                Native.Companion.getPtr(font),
                glyphs,
                floatPos,
                bounds
            )
            this
        } finally {
            Reference.reachabilityFence(font)
        }
    }

    fun appendRunRSXform(font: Font?, glyphs: ShortArray, xform: Array<RSXform>): TextBlobBuilder {
        return try {
            assert(glyphs.size == xform.size) { "glyphs.length " + glyphs.size + " != xform.length " + xform.size }
            val floatXform = FloatArray(xform.size * 4)
            for (i in xform.indices) {
                floatXform[i * 4] = xform[i]._scos
                floatXform[i * 4 + 1] = xform[i]._ssin
                floatXform[i * 4 + 2] = xform[i]._tx
                floatXform[i * 4 + 3] = xform[i]._ty
            }
            Stats.onNativeCall()
            _nAppendRunRSXform(
                _ptr,
                Native.Companion.getPtr(font),
                glyphs,
                floatXform
            )
            this
        } finally {
            Reference.reachabilityFence(font)
        }
    }

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}