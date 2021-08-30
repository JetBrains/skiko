package org.jetbrains.skija.svg

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.skija.impl.Managed.CleanerThunk
import org.jetbrains.skija.paragraph.Shadow
import org.jetbrains.skija.paragraph.TextBox
import org.jetbrains.skija.paragraph.Affinity
import org.jetbrains.skija.paragraph.Paragraph
import org.jetbrains.skija.paragraph.HeightMode
import org.jetbrains.skija.paragraph.StrutStyle
import org.jetbrains.skija.paragraph.BaselineMode
import org.jetbrains.skija.paragraph.RectWidthMode
import org.jetbrains.skija.paragraph.FontCollection
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
import org.jetbrains.skija.shaper.FontRun
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
import org.jetbrains.skija.svg.SVGDOM
import org.jetbrains.skija.svg.SVGSVG
import org.jetbrains.skija.svg.SVGTag
import org.jetbrains.skija.svg.SVGNode
import org.jetbrains.skija.svg.SVGCanvas
import org.jetbrains.skija.svg.SVGLength
import org.jetbrains.skija.svg.SVGLengthType
import org.jetbrains.skija.svg.SVGLengthUnit
import org.jetbrains.skija.svg.SVGLengthContext
import org.jetbrains.skija.svg.SVGPreserveAspectRatio
import org.jetbrains.skija.svg.SVGPreserveAspectRatioAlign
import org.jetbrains.skija.svg.SVGPreserveAspectRatioScale
import org.jetbrains.skija.ColorFilter._LinearToSRGBGammaHolder
import org.jetbrains.skija.ColorFilter._SRGBToLinearGammaHolder
import org.jetbrains.skija.ColorFilter._LumaHolder
import org.jetbrains.skija.ColorSpace._SRGBHolder
import org.jetbrains.skija.ColorSpace._SRGBLinearHolder
import org.jetbrains.skija.ColorSpace._DisplayP3Holder
import org.jetbrains.annotations.ApiStatus.NonExtendable
import org.jetbrains.annotations.Contract
import org.jetbrains.skija.*
import org.jetbrains.skija.FontMgr._DefaultHolder
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class SVGSVG @ApiStatus.Internal constructor(ptr: Long) : SVGContainer(ptr) {
    companion object {
        @ApiStatus.Internal
        external fun _nGetX(ptr: Long): SVGLength
        @ApiStatus.Internal
        external fun _nGetY(ptr: Long): SVGLength
        @ApiStatus.Internal
        external fun _nGetWidth(ptr: Long): SVGLength
        @ApiStatus.Internal
        external fun _nGetHeight(ptr: Long): SVGLength
        @ApiStatus.Internal
        external fun _nGetPreserveAspectRatio(ptr: Long): SVGPreserveAspectRatio
        @ApiStatus.Internal
        external fun _nGetViewBox(ptr: Long): Rect?
        @ApiStatus.Internal
        external fun _nGetIntrinsicSize(ptr: Long, width: Float, height: Float, dpi: Float): Point
        @ApiStatus.Internal
        external fun _nSetX(ptr: Long, value: Float, unit: Int)
        @ApiStatus.Internal
        external fun _nSetY(ptr: Long, value: Float, unit: Int)
        @ApiStatus.Internal
        external fun _nSetWidth(ptr: Long, value: Float, unit: Int)
        @ApiStatus.Internal
        external fun _nSetHeight(ptr: Long, value: Float, unit: Int)
        @ApiStatus.Internal
        external fun _nSetPreserveAspectRatio(ptr: Long, align: Int, scale: Int)
        @ApiStatus.Internal
        external fun _nSetViewBox(ptr: Long, l: Float, t: Float, r: Float, b: Float)

        init {
            staticLoad()
        }
    }

    val x: org.jetbrains.skija.svg.SVGLength
        get() = try {
            Stats.onNativeCall()
            _nGetX(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val y: org.jetbrains.skija.svg.SVGLength
        get() = try {
            Stats.onNativeCall()
            _nGetY(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val width: org.jetbrains.skija.svg.SVGLength
        get() = try {
            Stats.onNativeCall()
            _nGetWidth(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val height: org.jetbrains.skija.svg.SVGLength
        get() = try {
            Stats.onNativeCall()
            _nGetHeight(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val preserveAspectRatio: org.jetbrains.skija.svg.SVGPreserveAspectRatio
        get() = try {
            Stats.onNativeCall()
            _nGetPreserveAspectRatio(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    val viewBox: Rect?
        get() = try {
            Stats.onNativeCall()
            _nGetViewBox(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun getIntrinsicSize(lc: SVGLengthContext): Point {
        return try {
            Stats.onNativeCall()
            _nGetIntrinsicSize(_ptr, lc._width, lc._height, lc._dpi)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    @Contract("_ -> this")
    fun setX(length: SVGLength): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetX(_ptr, length._value, length._unit.ordinal())
            this
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    @Contract("_ -> this")
    fun setY(length: SVGLength): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetY(_ptr, length._value, length._unit.ordinal())
            this
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    @Contract("_ -> this")
    fun setWidth(length: SVGLength): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetWidth(_ptr, length._value, length._unit.ordinal())
            this
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    @Contract("_ -> this")
    fun setHeight(length: SVGLength): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetHeight(_ptr, length._value, length._unit.ordinal())
            this
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    @Contract("_ -> this")
    fun setPreserveAspectRatio(ratio: SVGPreserveAspectRatio): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetPreserveAspectRatio(_ptr, ratio._align._value, ratio._scale.ordinal())
            this
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    @Contract("_ -> this")
    fun setViewBox(viewBox: Rect): SVGSVG {
        return try {
            Stats.onNativeCall()
            _nSetViewBox(_ptr, viewBox._left, viewBox._top, viewBox._right, viewBox._bottom)
            this
        } finally {
            Reference.reachabilityFence(this)
        }
    }
}