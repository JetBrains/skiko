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
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class PictureRecorder @ApiStatus.Internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        // TODO
        /**
         *
         * Signal that the caller is done recording. This invalidates the canvas returned by
         * [.beginRecording]/[.getRecordingCanvas].
         *
         *
         * Unlike [.finishRecordingAsPicture], which returns an immutable picture,
         * the returned drawable may contain live references to other drawables (if they were added to
         * the recording canvas) and therefore this drawable will reflect the current state of those
         * nested drawables anytime it is drawn or a new picture is snapped from it (by calling
         * [Drawable.makePictureSnapshot]).
         */
        // public Drawable finishRecordingAsPicture(@NotNull Rect cull) {
        //     Stats.onNativeCall();
        //     return new Drawable(_nFinishRecordingAsDrawable(_ptr, 0));
        // }
        @ApiStatus.Internal
        external fun _nMake(): Long
        @ApiStatus.Internal
        external fun _nGetFinalizer(): Long
        @ApiStatus.Internal
        external fun _nBeginRecording(ptr: Long, left: Float, top: Float, right: Float, bottom: Float): Long
        @ApiStatus.Internal
        external fun _nGetRecordingCanvas(ptr: Long): Long
        @ApiStatus.Internal
        external fun _nFinishRecordingAsPicture(ptr: Long): Long
        @ApiStatus.Internal
        external fun _nFinishRecordingAsPictureWithCull(
            ptr: Long,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float
        ): Long

        @ApiStatus.Internal
        external fun _nFinishRecordingAsDrawable(ptr: Long): Long

        init {
            staticLoad()
        }
    }

    constructor() : this(_nMake()) {
        Stats.onNativeCall()
    }

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    /**
     * Returns the canvas that records the drawing commands.
     *
     * @param bounds the cull rect used when recording this picture. Any drawing the falls outside
     * of this rect is undefined, and may be drawn or it may not.
     * @return the canvas.
     */
    fun beginRecording(bounds: Rect): Canvas {
        return try {
            Stats.onNativeCall()
            org.jetbrains.skija.Canvas(
                _nBeginRecording(
                    _ptr,
                    bounds._left,
                    bounds._top,
                    bounds._right,
                    bounds._bottom
                ), false, this
            )
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    /**
     * @return  the recording canvas if one is active, or null if recording is not active.
     */
    val recordingCanvas: Canvas?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetRecordingCanvas(_ptr)
            if (ptr == 0L) null else org.jetbrains.skija.Canvas(ptr, false, this)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     *
     * Signal that the caller is done recording. This invalidates the canvas returned by
     * [.beginRecording]/[.getRecordingCanvas].
     *
     *
     * The returned picture is immutable. If during recording drawables were added to the canvas,
     * these will have been "drawn" into a recording canvas, so that this resulting picture will
     * reflect their current state, but will not contain a live reference to the drawables
     * themselves.
     */
    fun finishRecordingAsPicture(): Picture {
        return try {
            Stats.onNativeCall()
            Picture(_nFinishRecordingAsPicture(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    /**
     * Signal that the caller is done recording, and update the cull rect to use for bounding
     * box hierarchy (BBH) generation. The behavior is the same as calling
     * [.finishRecordingAsPicture], except that this method updates the cull rect
     * initially passed into [.beginRecording].
     *
     * @param cull the new culling rectangle to use as the overall bound for BBH generation
     * and subsequent culling operations.
     * @return the picture containing the recorded content.
     */
    fun finishRecordingAsPicture(cull: Rect): Picture {
        return try {
            Stats.onNativeCall()
            Picture(
                _nFinishRecordingAsPictureWithCull(
                    _ptr,
                    cull._left,
                    cull._top,
                    cull._right,
                    cull._bottom
                )
            )
        } finally {
            Reference.reachabilityFence(this)
        }
    }
}