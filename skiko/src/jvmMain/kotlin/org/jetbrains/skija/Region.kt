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

class Region : Managed(_nMake(), _FinalizerHolder.PTR) {
    companion object {
        external fun _nMake(): Long
        external fun _nGetFinalizer(): Long
        external fun _nSet(ptr: Long, regoinPtr: Long): Boolean
        external fun _nIsEmpty(ptr: Long): Boolean
        external fun _nIsRect(ptr: Long): Boolean
        external fun _nIsComplex(ptr: Long): Boolean
        external fun _nGetBounds(ptr: Long): IRect
        external fun _nComputeRegionComplexity(ptr: Long): Int
        external fun _nGetBoundaryPath(ptr: Long, pathPtr: Long): Boolean
        external fun _nSetEmpty(ptr: Long): Boolean
        external fun _nSetRect(ptr: Long, left: Int, top: Int, right: Int, bottom: Int): Boolean
        external fun _nSetRects(ptr: Long, rects: IntArray?): Boolean
        external fun _nSetRegion(ptr: Long, regionPtr: Long): Boolean
        external fun _nSetPath(ptr: Long, pathPtr: Long, regionPtr: Long): Boolean
        external fun _nIntersectsIRect(ptr: Long, left: Int, top: Int, right: Int, bottom: Int): Boolean
        external fun _nIntersectsRegion(ptr: Long, regionPtr: Long): Boolean
        external fun _nContainsIPoint(ptr: Long, x: Int, y: Int): Boolean
        external fun _nContainsIRect(ptr: Long, left: Int, top: Int, right: Int, bottom: Int): Boolean
        external fun _nContainsRegion(ptr: Long, regionPtr: Long): Boolean
        external fun _nQuickContains(ptr: Long, left: Int, top: Int, right: Int, bottom: Int): Boolean
        external fun _nQuickRejectIRect(ptr: Long, left: Int, top: Int, right: Int, bottom: Int): Boolean
        external fun _nQuickRejectRegion(ptr: Long, regionPtr: Long): Boolean
        external fun _nTranslate(ptr: Long, dx: Int, dy: Int)
        external fun _nOpIRect(ptr: Long, left: Int, top: Int, right: Int, bottom: Int, op: Int): Boolean
        external fun _nOpRegion(ptr: Long, regionPtr: Long, op: Int): Boolean
        external fun _nOpIRectRegion(
            ptr: Long,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            regionPtr: Long,
            op: Int
        ): Boolean

        external fun _nOpRegionIRect(
            ptr: Long,
            regionPtr: Long,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            op: Int
        ): Boolean

        external fun _nOpRegionRegion(ptr: Long, regionPtrA: Long, regionPtrB: Long, op: Int): Boolean

        init {
            staticLoad()
        }
    }

    enum class Op {
        DIFFERENCE, INTERSECT, UNION, XOR, REVERSE_DIFFERENCE, REPLACE;

        companion object {
            @ApiStatus.Internal
            val _values = values()
        }
    }

    fun set(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            _nSet(_ptr, Native.Companion.getPtr(r))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(r)
        }
    }

    val isEmpty: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsEmpty(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val isRect: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsRect(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val isComplex: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsComplex(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val bounds: IRect
        get() = try {
            Stats.onNativeCall()
            _nGetBounds(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun computeRegionComplexity(): Int {
        return try {
            Stats.onNativeCall()
            _nComputeRegionComplexity(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun getBoundaryPath(p: Path?): Boolean {
        return try {
            Stats.onNativeCall()
            _nGetBoundaryPath(
                _ptr,
                Native.Companion.getPtr(p)
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(p)
        }
    }

    fun setEmpty(): Boolean {
        return try {
            Stats.onNativeCall()
            _nSetEmpty(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun setRect(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            _nSetRect(_ptr, rect._left, rect._top, rect._right, rect._bottom)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun setRects(rects: Array<IRect>): Boolean {
        return try {
            val arr = IntArray(rects.size * 4)
            for (i in rects.indices) {
                arr[i * 4] = rects[i]._left
                arr[i * 4 + 1] = rects[i]._top
                arr[i * 4 + 2] = rects[i]._right
                arr[i * 4 + 3] = rects[i]._bottom
            }
            Stats.onNativeCall()
            _nSetRects(_ptr, arr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun setRegion(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            _nSetRegion(_ptr, Native.Companion.getPtr(r))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(r)
        }
    }

    fun setPath(path: Path?, clip: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            _nSetPath(
                _ptr,
                Native.Companion.getPtr(path),
                Native.Companion.getPtr(clip)
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(path)
            Reference.reachabilityFence(clip)
        }
    }

    fun intersects(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            _nIntersectsIRect(_ptr, rect._left, rect._top, rect._right, rect._bottom)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun intersects(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            _nIntersectsRegion(
                _ptr,
                Native.Companion.getPtr(r)
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(r)
        }
    }

    fun contains(x: Int, y: Int): Boolean {
        return try {
            Stats.onNativeCall()
            _nContainsIPoint(_ptr, x, y)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    operator fun contains(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            _nContainsIRect(_ptr, rect._left, rect._top, rect._right, rect._bottom)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    operator fun contains(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            _nContainsRegion(_ptr, Native.Companion.getPtr(r))
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(r)
        }
    }

    fun quickContains(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            _nQuickContains(_ptr, rect._left, rect._top, rect._right, rect._bottom)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun quickReject(rect: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            _nQuickRejectIRect(_ptr, rect._left, rect._top, rect._right, rect._bottom)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun quickReject(r: Region?): Boolean {
        return try {
            Stats.onNativeCall()
            _nQuickRejectRegion(
                _ptr,
                Native.Companion.getPtr(r)
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(r)
        }
    }

    fun translate(dx: Int, dy: Int) {
        try {
            Stats.onNativeCall()
            _nTranslate(_ptr, dx, dy)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun op(rect: IRect, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            _nOpIRect(
                _ptr,
                rect._left,
                rect._top,
                rect._right,
                rect._bottom,
                op.ordinal
            )
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun op(r: Region?, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            _nOpRegion(
                _ptr,
                Native.Companion.getPtr(r),
                op.ordinal
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(r)
        }
    }

    fun op(rect: IRect, r: Region?, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            _nOpIRectRegion(
                _ptr,
                rect._left,
                rect._top,
                rect._right,
                rect._bottom,
                Native.Companion.getPtr(r),
                op.ordinal
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(r)
        }
    }

    fun op(r: Region?, rect: IRect, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            _nOpRegionIRect(
                _ptr,
                Native.Companion.getPtr(r),
                rect._left,
                rect._top,
                rect._right,
                rect._bottom,
                op.ordinal
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(r)
        }
    }

    fun op(a: Region?, b: Region?, op: Op): Boolean {
        return try {
            Stats.onNativeCall()
            _nOpRegionRegion(
                _ptr,
                Native.Companion.getPtr(a),
                Native.Companion.getPtr(b),
                op.ordinal
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(a)
            Reference.reachabilityFence(b)
        }
    }

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    init {
        Stats.onNativeCall()
    }
}