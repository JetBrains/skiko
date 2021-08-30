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
import java.util.function.BooleanSupplier

class Picture @ApiStatus.Internal constructor(ptr: Long) : RefCnt(ptr) {
    companion object {
        /**
         * Recreates Picture that was serialized into data. Returns constructed Picture
         * if successful; otherwise, returns null. Fails if data does not permit
         * constructing valid Picture.
         */
        fun makeFromData(data: Data?): Picture? {
            return try {
                Stats.onNativeCall()
                val ptr = _nMakeFromData(Native.Companion.getPtr(data))
                if (ptr == 0L) null else Picture(ptr)
            } finally {
                Reference.reachabilityFence(data)
            }
        }

        /**
         *
         * Returns a placeholder Picture. Result does not draw, and contains only
         * cull Rect, a hint of its bounds. Result is immutable; it cannot be changed
         * later. Result identifier is unique.
         *
         *
         * Returned placeholder can be intercepted during playback to insert other
         * commands into Canvas draw stream.
         *
         * @param cull  placeholder dimensions
         * @return      placeholder with unique identifier
         *
         * @see [https://fiddle.skia.org/c/@Picture_MakePlaceholder](https://fiddle.skia.org/c/@Picture_MakePlaceholder)
         */
        fun makePlaceholder(cull: Rect): Picture {
            Stats.onNativeCall()
            return Picture(_nMakePlaceholder(cull._left, cull._top, cull._right, cull._bottom))
        }

        @ApiStatus.Internal
        external fun _nMakeFromData(dataPtr: Long /*, SkDeserialProcs */): Long
        @ApiStatus.Internal
        external fun _nPlayback(ptr: Long, canvasPtr: Long, abort: BooleanSupplier?)
        @ApiStatus.Internal
        external fun _nGetCullRect(ptr: Long): Rect
        @ApiStatus.Internal
        external fun _nGetUniqueId(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nSerializeToData(ptr: Long /*, SkSerialProcs */): Long
        @ApiStatus.Internal
        external fun _nMakePlaceholder(left: Float, top: Float, right: Float, bottom: Float): Long
        @ApiStatus.Internal
        external fun _nGetApproximateOpCount(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nGetApproximateBytesUsed(ptr: Long): Long
        @ApiStatus.Internal
        external fun _nMakeShader(
            ptr: Long,
            tmx: Int,
            tmy: Int,
            filterMode: Int,
            localMatrix: FloatArray?,
            tileRect: Rect?
        ): Long

        init {
            staticLoad()
        }
    }
    /**
     *
     * Replays the drawing commands on the specified canvas. In the case that the
     * commands are recorded, each command in the Picture is sent separately to canvas.
     *
     *
     * To add a single command to draw Picture to recording canvas, call
     * [Canvas.drawPicture] instead.
     *
     * @param canvas  receiver of drawing commands
     * @param abort   return true to interrupt the playback
     * @return        this
     *
     * @see [https://fiddle.skia.org/c/@Picture_playback](https://fiddle.skia.org/c/@Picture_playback)
     */
    /**
     *
     * Replays the drawing commands on the specified canvas. In the case that the
     * commands are recorded, each command in the Picture is sent separately to canvas.
     *
     *
     * To add a single command to draw Picture to recording canvas, call
     * [Canvas.drawPicture] instead.
     *
     * @param canvas  receiver of drawing commands
     * @return        this
     *
     * @see [https://fiddle.skia.org/c/@Picture_playback](https://fiddle.skia.org/c/@Picture_playback)
     */
    @JvmOverloads
    fun playback(canvas: Canvas?, abort: BooleanSupplier? = null): Picture {
        return try {
            Stats.onNativeCall()
            _nPlayback(_ptr, Native.Companion.getPtr(canvas), abort)
            this
        } finally {
            Reference.reachabilityFence(canvas)
        }
    }

    /**
     *
     * Returns cull Rect for this picture, passed in when Picture was created.
     * Returned Rect does not specify clipping Rect for Picture; cull is hint
     * of Picture bounds.
     *
     *
     * Picture is free to discard recorded drawing commands that fall outside cull.
     *
     * @return  bounds passed when Picture was created
     *
     * @see [https://fiddle.skia.org/c/@Picture_cullRect](https://fiddle.skia.org/c/@Picture_cullRect)
     */
    val cullRect: Rect
        get() = try {
            Stats.onNativeCall()
            _nGetCullRect(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * Returns a non-zero value unique among Picture in Skia process.
     *
     * @return  identifier for Picture
     */
    val uniqueId: Int
        get() = try {
            Stats.onNativeCall()
            _nGetUniqueId(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * @return  storage containing Data describing Picture.
     *
     * @see [https://fiddle.skia.org/c/@Picture_serialize](https://fiddle.skia.org/c/@Picture_serialize)
     */
    fun serializeToData(): Data {
        return try {
            Stats.onNativeCall()
            org.jetbrains.skija.Data(_nSerializeToData(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    /**
     * Returns the approximate number of operations in SkPicture. Returned value
     * may be greater or less than the number of SkCanvas calls
     * recorded: some calls may be recorded as more than one operation, other
     * calls may be optimized away.
     *
     * @return  approximate operation count
     *
     * @see [https://fiddle.skia.org/c/@Picture_approximateOpCount](https://fiddle.skia.org/c/@Picture_approximateOpCount)
     */
    val approximateOpCount: Int
        get() = try {
            Stats.onNativeCall()
            _nGetApproximateOpCount(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * Returns the approximate byte size of Picture. Does not include large objects
     * referenced by Picture.
     *
     * @return  approximate size
     *
     * @see [https://fiddle.skia.org/c/@Picture_approximateBytesUsed](https://fiddle.skia.org/c/@Picture_approximateBytesUsed)
     */
    val approximateBytesUsed: Long
        get() = try {
            Stats.onNativeCall()
            _nGetApproximateBytesUsed(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    /**
     * Return a new shader that will draw with this picture.
     *
     * @param tmx          The tiling mode to use when sampling in the x-direction.
     * @param tmy          The tiling mode to use when sampling in the y-direction.
     * @param mode         How to filter the tiles
     * @param localMatrix  Optional matrix used when sampling
     * @param tileRect     The tile rectangle in picture coordinates: this represents the subset
     * (or superset) of the picture used when building a tile. It is not
     * affected by localMatrix and does not imply scaling (only translation
     * and cropping). If null, the tile rect is considered equal to the picture
     * bounds.
     * @return             Returns a new shader object. Note: this function never returns null.
     */
    /**
     * Return a new shader that will draw with this picture. The tile rect is considered
     * equal to the picture bounds.
     *
     * @param tmx   The tiling mode to use when sampling in the x-direction.
     * @param tmy   The tiling mode to use when sampling in the y-direction.
     * @param mode  How to filter the tiles
     * @return      Returns a new shader object. Note: this function never returns null.
     */
    /**
     * Return a new shader that will draw with this picture. The tile rect is considered
     * equal to the picture bounds.
     *
     * @param tmx          The tiling mode to use when sampling in the x-direction.
     * @param tmy          The tiling mode to use when sampling in the y-direction.
     * @param mode         How to filter the tiles
     * @param localMatrix  Optional matrix used when sampling
     * @return             Returns a new shader object. Note: this function never returns null.
     */
    @JvmOverloads
    fun makeShader(
        tmx: FilterTileMode,
        tmy: FilterTileMode,
        mode: FilterMode,
        localMatrix: Matrix33? = null,
        tileRect: Rect? = null
    ): Shader {
        return try {
            Stats.onNativeCall()
            val arr = localMatrix?._mat
            Shader(_nMakeShader(_ptr, tmx.ordinal, tmy.ordinal, mode.ordinal, arr, tileRect))
        } finally {
            Reference.reachabilityFence(this)
        }
    }
}