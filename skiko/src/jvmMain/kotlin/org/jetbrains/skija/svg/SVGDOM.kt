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
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class SVGDOM @ApiStatus.Internal constructor(ptr: Long) : RefCnt(ptr) {
    companion object {
        @ApiStatus.Internal
        external fun _nMakeFromData(dataPtr: Long): Long
        @ApiStatus.Internal
        external fun _nGetRoot(ptr: Long): Long
        @ApiStatus.Internal
        external fun _nGetContainerSize(ptr: Long): Point
        @ApiStatus.Internal
        external fun _nSetContainerSize(ptr: Long, width: Float, height: Float)
        @ApiStatus.Internal
        external fun _nRender(ptr: Long, canvasPtr: Long)

        init {
            staticLoad()
        }
    }

    constructor(data: Data) : this(_nMakeFromData(Native.Companion.getPtr(data))) {
        Stats.onNativeCall()
        Reference.reachabilityFence(data)
    }

    val root: org.jetbrains.skija.svg.SVGSVG?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetRoot(_ptr)
            if (ptr == 0L) null else SVGSVG(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * Deprecated. Use getRoot().intrinsicSize() instead
     */
    @get:Deprecated("")
    val containerSize: Point
        get() = try {
            _nGetContainerSize(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    @Contract("-> this")
    fun setContainerSize(width: Float, height: Float): SVGDOM {
        Stats.onNativeCall()
        _nSetContainerSize(_ptr, width, height)
        return this
    }

    @Contract("-> this")
    fun setContainerSize(size: Point): SVGDOM {
        Stats.onNativeCall()
        _nSetContainerSize(_ptr, size._x, size._y)
        return this
    }

    // sk_sp<SkSVGNode>* findNodeById(const char* id);
    @Contract("-> this")
    fun render(canvas: Canvas): SVGDOM {
        return try {
            Stats.onNativeCall()
            _nRender(_ptr, Native.Companion.getPtr(canvas))
            this
        } finally {
            Reference.reachabilityFence(canvas)
        }
    }
}