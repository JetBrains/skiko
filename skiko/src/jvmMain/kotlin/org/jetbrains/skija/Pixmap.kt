package org.jetbrains.skija

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
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
import org.jetbrains.skija.impl.*
import java.lang.ref.Reference
import java.nio.ByteBuffer

class Pixmap @ApiStatus.Internal constructor(ptr: Long, managed: Boolean) :
    Managed(ptr, _FinalizerHolder.PTR, managed) {
    constructor() : this(_nMakeNull(), true) {
        Stats.onNativeCall()
    }

    fun reset() {
        Stats.onNativeCall()
        _nReset(_ptr)
        Reference.reachabilityFence(this)
    }

    fun reset(info: ImageInfo, addr: Long, rowBytes: Int) {
        Stats.onNativeCall()
        _nResetWithInfo(
            _ptr,
            info._width, info._height,
            info._colorInfo._colorType.ordinal,
            info._colorInfo._alphaType.ordinal,
            Native.Companion.getPtr(info._colorInfo._colorSpace), addr, rowBytes
        )
        Reference.reachabilityFence(this)
        Reference.reachabilityFence(info._colorInfo._colorSpace)
    }

    fun reset(info: ImageInfo, buffer: ByteBuffer, rowBytes: Int) {
        reset(info, BufferUtil.getPointerFromByteBuffer(buffer), rowBytes)
    }

    fun setColorSpace(colorSpace: ColorSpace?) {
        Stats.onNativeCall()
        _nSetColorSpace(_ptr, Native.Companion.getPtr(colorSpace))
        Reference.reachabilityFence(this)
        Reference.reachabilityFence(colorSpace)
    }

    fun extractSubset(subsetPtr: Long, area: IRect): Boolean {
        return try {
            Stats.onNativeCall()
            _nExtractSubset(
                _ptr,
                subsetPtr,
                area._left,
                area._top,
                area._right,
                area._bottom
            )
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun extractSubset(buffer: ByteBuffer, area: IRect): Boolean {
        return extractSubset(BufferUtil.getPointerFromByteBuffer(buffer), area)
    }

    val info: ImageInfo
        get() {
            Stats.onNativeCall()
            return try {
                _nGetInfo(_ptr)
            } finally {
                Reference.reachabilityFence(this)
            }
        }
    val rowBytes: Int
        get() {
            Stats.onNativeCall()
            return try {
                _nGetRowBytes(_ptr)
            } finally {
                Reference.reachabilityFence(this)
            }
        }
    val addr: Long
        get() {
            Stats.onNativeCall()
            return try {
                _nGetAddr(_ptr)
            } finally {
                Reference.reachabilityFence(this)
            }
        }
    val rowBytesAsPixels: Int
        get() {
            Stats.onNativeCall()
            return try {
                _nGetRowBytesAsPixels(_ptr)
            } finally {
                Reference.reachabilityFence(this)
            }
        }

    fun computeByteSize(): Int {
        Stats.onNativeCall()
        return try {
            _nComputeByteSize(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun computeIsOpaque(): Boolean {
        Stats.onNativeCall()
        return try {
            _nComputeIsOpaque(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun getColor(x: Int, y: Int): Int {
        Stats.onNativeCall()
        return try {
            _nGetColor(_ptr, x, y)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun getAlphaF(x: Int, y: Int): Float {
        Stats.onNativeCall()
        return try {
            _nGetAlphaF(_ptr, x, y)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun getAddr(x: Int, y: Int): Long {
        Stats.onNativeCall()
        return try {
            _nGetAddrAt(_ptr, x, y)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun readPixels(info: ImageInfo, addr: Long, rowBytes: Int): Boolean {
        Stats.onNativeCall()
        return try {
            _nReadPixels(
                _ptr,
                info._width, info._height,
                info._colorInfo._colorType.ordinal,
                info._colorInfo._alphaType.ordinal,
                Native.Companion.getPtr(info._colorInfo._colorSpace), addr, rowBytes
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(info._colorInfo._colorSpace)
        }
    }

    fun readPixels(info: ImageInfo, addr: Long, rowBytes: Int, srcX: Int, srcY: Int): Boolean {
        Stats.onNativeCall()
        return try {
            _nReadPixelsFromPoint(
                _ptr,
                info._width, info._height,
                info._colorInfo._colorType.ordinal,
                info._colorInfo._alphaType.ordinal,
                Native.Companion.getPtr(info._colorInfo._colorSpace), addr, rowBytes,
                srcX, srcY
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(info._colorInfo._colorSpace)
        }
    }

    fun readPixels(pixmap: Pixmap?): Boolean {
        Stats.onNativeCall()
        return try {
            _nReadPixelsToPixmap(
                _ptr,
                Native.Companion.getPtr(pixmap)
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(pixmap)
        }
    }

    fun readPixels(pixmap: Pixmap?, srcX: Int, srcY: Int): Boolean {
        Stats.onNativeCall()
        return try {
            _nReadPixelsToPixmapFromPoint(
                _ptr,
                Native.Companion.getPtr(pixmap),
                srcX,
                srcY
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(pixmap)
        }
    }

    fun scalePixels(dstPixmap: Pixmap?, samplingMode: SamplingMode): Boolean {
        Stats.onNativeCall()
        return try {
            _nScalePixels(
                _ptr,
                Native.Companion.getPtr(dstPixmap),
                samplingMode._pack()
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(dstPixmap)
        }
    }

    fun erase(color: Int): Boolean {
        Stats.onNativeCall()
        return try {
            _nErase(_ptr, color)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun erase(color: Int, subset: IRect): Boolean {
        Stats.onNativeCall()
        return try {
            _nEraseSubset(
                _ptr,
                color,
                subset._left,
                subset._top,
                subset._right,
                subset._bottom
            )
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    val buffer: ByteBuffer?
        get() = BufferUtil.getByteBufferFromPointer(addr, computeByteSize())

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    companion object {
        fun make(info: ImageInfo, buffer: ByteBuffer, rowBytes: Int): Pixmap {
            return make(info, BufferUtil.getPointerFromByteBuffer(buffer), rowBytes)
        }

        fun make(info: ImageInfo, addr: Long, rowBytes: Int): Pixmap {
            return try {
                val ptr = _nMake(
                    info._width, info._height,
                    info._colorInfo._colorType.ordinal,
                    info._colorInfo._alphaType.ordinal,
                    Native.Companion.getPtr(info._colorInfo._colorSpace), addr, rowBytes
                )
                require(ptr != 0L) { "Failed to create Pixmap." }
                Pixmap(ptr, true)
            } finally {
                Reference.reachabilityFence(info._colorInfo._colorSpace)
            }
        }

        external fun _nGetFinalizer(): Long
        external fun _nMakeNull(): Long
        external fun _nMake(
            width: Int,
            height: Int,
            colorType: Int,
            alphaType: Int,
            colorSpacePtr: Long,
            pixelsPtr: Long,
            rowBytes: Int
        ): Long

        external fun _nReset(ptr: Long)
        external fun _nResetWithInfo(
            ptr: Long,
            width: Int,
            height: Int,
            colorType: Int,
            alphaType: Int,
            colorSpacePtr: Long,
            pixelsPtr: Long,
            rowBytes: Int
        )

        external fun _nSetColorSpace(ptr: Long, colorSpacePtr: Long)
        external fun _nExtractSubset(ptr: Long, subsetPtr: Long, l: Int, t: Int, r: Int, b: Int): Boolean
        external fun _nGetInfo(ptr: Long): ImageInfo
        external fun _nGetRowBytes(ptr: Long): Int
        external fun _nGetAddr(ptr: Long): Long

        // TODO methods flattening ImageInfo not included yet - use GetInfo() instead.
        external fun _nGetRowBytesAsPixels(ptr: Long): Int

        // TODO shiftPerPixel
        external fun _nComputeByteSize(ptr: Long): Int
        external fun _nComputeIsOpaque(ptr: Long): Boolean
        external fun _nGetColor(ptr: Long, x: Int, y: Int): Int
        external fun _nGetAlphaF(ptr: Long, x: Int, y: Int): Float
        external fun _nGetAddrAt(ptr: Long, x: Int, y: Int): Long

        // methods related to C++ types(addr8/16/32/64, writable_addr8/16/32/64) not included - not needed
        external fun _nReadPixels(
            ptr: Long,
            width: Int,
            height: Int,
            colorType: Int,
            alphaType: Int,
            colorSpacePtr: Long,
            dstPixelsPtr: Long,
            dstRowBytes: Int
        ): Boolean

        external fun _nReadPixelsFromPoint(
            ptr: Long,
            width: Int,
            height: Int,
            colorType: Int,
            alphaType: Int,
            colorSpacePtr: Long,
            dstPixelsPtr: Long,
            dstRowBytes: Int,
            srcX: Int,
            srcY: Int
        ): Boolean

        external fun _nReadPixelsToPixmap(ptr: Long, dstPixmapPtr: Long): Boolean
        external fun _nReadPixelsToPixmapFromPoint(ptr: Long, dstPixmapPtr: Long, srcX: Int, srcY: Int): Boolean
        external fun _nScalePixels(ptr: Long, dstPixmapPtr: Long, samplingOptions: Long): Boolean
        external fun _nErase(ptr: Long, color: Int): Boolean
        external fun _nEraseSubset(
            ptr: Long,
            color: Int,
            l: Int,
            t: Int,
            r: Int,
            b: Int
        ): Boolean // TODO float erase methods not included
    }
}