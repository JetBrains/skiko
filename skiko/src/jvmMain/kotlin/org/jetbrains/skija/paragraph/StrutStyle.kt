package org.jetbrains.skija.paragraph

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
import org.jetbrains.skija.*
import org.jetbrains.skija.FontMgr._DefaultHolder
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class StrutStyle @ApiStatus.Internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @ApiStatus.Internal
        external fun _nGetFinalizer(): Long
        @ApiStatus.Internal
        external fun _nMake(): Long
        @ApiStatus.Internal
        external fun _nEquals(ptr: Long, otherPtr: Long): Boolean
        @ApiStatus.Internal
        external fun _nGetFontFamilies(ptr: Long): Array<String>
        @ApiStatus.Internal
        external fun _nSetFontFamilies(ptr: Long, families: Array<String?>?)
        @ApiStatus.Internal
        external fun _nGetFontStyle(ptr: Long): Int
        @ApiStatus.Internal
        external fun _nSetFontStyle(ptr: Long, value: Int)
        @ApiStatus.Internal
        external fun _nGetFontSize(ptr: Long): Float
        @ApiStatus.Internal
        external fun _nSetFontSize(ptr: Long, value: Float)
        @ApiStatus.Internal
        external fun _nGetHeight(ptr: Long): Float
        @ApiStatus.Internal
        external fun _nSetHeight(ptr: Long, value: Float)
        @ApiStatus.Internal
        external fun _nGetLeading(ptr: Long): Float
        @ApiStatus.Internal
        external fun _nSetLeading(ptr: Long, value: Float)
        @ApiStatus.Internal
        external fun _nIsEnabled(ptr: Long): Boolean
        @ApiStatus.Internal
        external fun _nSetEnabled(ptr: Long, value: Boolean)
        @ApiStatus.Internal
        external fun _nIsHeightForced(ptr: Long): Boolean
        @ApiStatus.Internal
        external fun _nSetHeightForced(ptr: Long, value: Boolean)
        @ApiStatus.Internal
        external fun _nIsHeightOverridden(ptr: Long): Boolean
        @ApiStatus.Internal
        external fun _nSetHeightOverridden(ptr: Long, value: Boolean)

        init {
            staticLoad()
        }
    }

    constructor() : this(_nMake()) {
        Stats.onNativeCall()
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

    val fontFamilies: Array<String>
        get() = try {
            Stats.onNativeCall()
            _nGetFontFamilies(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setFontFamilies(families: Array<String?>?): StrutStyle {
        Stats.onNativeCall()
        _nSetFontFamilies(_ptr, families)
        return this
    }

    val fontStyle: FontStyle
        get() = try {
            Stats.onNativeCall()
            org.jetbrains.skija.FontStyle(_nGetFontStyle(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setFontStyle(style: FontStyle): StrutStyle {
        Stats.onNativeCall()
        _nSetFontStyle(_ptr, style._value)
        return this
    }

    val fontSize: Float
        get() = try {
            Stats.onNativeCall()
            _nGetFontSize(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setFontSize(value: Float): StrutStyle {
        Stats.onNativeCall()
        _nSetFontSize(_ptr, value)
        return this
    }

    val height: Float
        get() = try {
            Stats.onNativeCall()
            _nGetHeight(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setHeight(value: Float): StrutStyle {
        Stats.onNativeCall()
        _nSetHeight(_ptr, value)
        return this
    }

    val leading: Float
        get() = try {
            Stats.onNativeCall()
            _nGetLeading(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setLeading(value: Float): StrutStyle {
        Stats.onNativeCall()
        _nSetLeading(_ptr, value)
        return this
    }

    val isEnabled: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsEnabled(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setEnabled(value: Boolean): StrutStyle {
        Stats.onNativeCall()
        _nSetEnabled(_ptr, value)
        return this
    }

    val isHeightForced: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsHeightForced(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setHeightForced(value: Boolean): StrutStyle {
        Stats.onNativeCall()
        _nSetHeightForced(_ptr, value)
        return this
    }

    val isHeightOverridden: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsHeightOverridden(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setHeightOverridden(value: Boolean): StrutStyle {
        Stats.onNativeCall()
        _nSetHeightOverridden(_ptr, value)
        return this
    }

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}