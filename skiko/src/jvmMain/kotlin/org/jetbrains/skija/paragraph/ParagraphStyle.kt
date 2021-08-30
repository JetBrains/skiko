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
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class ParagraphStyle : Managed(_nMake(), _FinalizerHolder.PTR) {
    companion object {
        @ApiStatus.Internal
        external fun _nGetFinalizer(): Long
        @ApiStatus.Internal
        external fun _nMake(): Long
        @ApiStatus.Internal
        external fun _nEquals(ptr: Long, otherPtr: Long): Boolean
        @ApiStatus.Internal
        external fun _nGetStrutStyle(ptr: Long): Long
        @ApiStatus.Internal
        external fun _nSetStrutStyle(ptr: Long, stylePtr: Long)
        @ApiStatus.Internal
        external fun _nGetTextStyle(ptr: Long): Long
        @ApiStatus.Internal
        external fun _nSetTextStyle(ptr: Long, textStylePtr: Long)
        @ApiStatus.Internal
        external fun _nGetDirection(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nSetDirection(ptr: Long, direction: Int)
        @ApiStatus.Internal
        external fun _nGetAlignment(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nSetAlignment(ptr: Long, align: Int)
        @ApiStatus.Internal
        external fun _nGetMaxLinesCount(ptr: Long): Long
        @ApiStatus.Internal
        external fun _nSetMaxLinesCount(ptr: Long, maxLines: Long)
        @ApiStatus.Internal
        external fun _nGetEllipsis(ptr: Long): String
        @ApiStatus.Internal
        external fun _nSetEllipsis(ptr: Long, ellipsis: String?)
        @ApiStatus.Internal
        external fun _nGetHeight(ptr: Long): Float
        @ApiStatus.Internal
        external fun _nSetHeight(ptr: Long, height: Float)
        @ApiStatus.Internal
        external fun _nGetHeightMode(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nSetHeightMode(ptr: Long, v: Int)
        @ApiStatus.Internal
        external fun _nGetEffectiveAlignment(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nIsHintingEnabled(ptr: Long): Boolean
        @ApiStatus.Internal
        external fun _nDisableHinting(ptr: Long)

        init {
            staticLoad()
        }
    }

    @ApiStatus.Internal
    override fun _nativeEquals(other: Native?): Boolean {
        return try {
            Stats.onNativeCall()
            _nEquals(_ptr, Native.Companion.getPtr(other))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(other)
        }
    }

    val strutStyle: org.jetbrains.skija.paragraph.StrutStyle
        get() = try {
            Stats.onNativeCall()
            StrutStyle(_nGetStrutStyle(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setStrutStyle(s: StrutStyle?): ParagraphStyle {
        return try {
            Stats.onNativeCall()
            _nSetStrutStyle(_ptr, Native.Companion.getPtr(s))
            this
        } finally {
            Reference.reachabilityFence(s)
        }
    }

    val textStyle: org.jetbrains.skija.paragraph.TextStyle
        get() = try {
            Stats.onNativeCall()
            TextStyle(_nGetTextStyle(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setTextStyle(style: TextStyle?): ParagraphStyle {
        return try {
            Stats.onNativeCall()
            _nSetTextStyle(_ptr, Native.Companion.getPtr(style))
            this
        } finally {
            Reference.reachabilityFence(style)
        }
    }

    val direction: org.jetbrains.skija.paragraph.Direction
        get() = try {
            Stats.onNativeCall()
            Direction._values.get(_nGetDirection(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setDirection(style: Direction): ParagraphStyle {
        Stats.onNativeCall()
        _nSetDirection(_ptr, style.ordinal())
        return this
    }

    val alignment: org.jetbrains.skija.paragraph.Alignment
        get() = try {
            Stats.onNativeCall()
            Alignment._values.get(_nGetAlignment(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setAlignment(alignment: Alignment): ParagraphStyle {
        Stats.onNativeCall()
        _nSetAlignment(_ptr, alignment.ordinal())
        return this
    }

    val maxLinesCount: Long
        get() = try {
            Stats.onNativeCall()
            _nGetMaxLinesCount(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setMaxLinesCount(count: Long): ParagraphStyle {
        Stats.onNativeCall()
        _nSetMaxLinesCount(_ptr, count)
        return this
    }

    val ellipsis: String
        get() = try {
            Stats.onNativeCall()
            _nGetEllipsis(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setEllipsis(ellipsis: String?): ParagraphStyle {
        Stats.onNativeCall()
        _nSetEllipsis(_ptr, ellipsis)
        return this
    }

    val height: Float
        get() = try {
            Stats.onNativeCall()
            _nGetHeight(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setHeight(height: Float): ParagraphStyle {
        Stats.onNativeCall()
        _nSetHeight(_ptr, height)
        return this
    }

    val heightMode: org.jetbrains.skija.paragraph.HeightMode
        get() = try {
            Stats.onNativeCall()
            HeightMode._values.get(_nGetHeightMode(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setHeightMode(behavior: HeightMode): ParagraphStyle {
        Stats.onNativeCall()
        _nSetHeightMode(_ptr, behavior.ordinal())
        return this
    }

    val effectiveAlignment: org.jetbrains.skija.paragraph.Alignment
        get() = try {
            Stats.onNativeCall()
            Alignment._values.get(_nGetEffectiveAlignment(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }
    val isHintingEnabled: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsHintingEnabled(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun disableHinting(): ParagraphStyle {
        Stats.onNativeCall()
        _nDisableHinting(_ptr)
        return this
    }

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    init {
        Stats.onNativeCall()
    }
}