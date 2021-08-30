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
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class FontCollection @ApiStatus.Internal constructor(ptr: Long) : RefCnt(ptr) {
    companion object {
        external fun _nMake(): Long
        external fun _nGetFontManagersCount(ptr: Long): Long
        external fun _nSetAssetFontManager(ptr: Long, fontManagerPtr: Long): Long
        external fun _nSetDynamicFontManager(ptr: Long, fontManagerPtr: Long): Long
        external fun _nSetTestFontManager(ptr: Long, fontManagerPtr: Long): Long
        external fun _nSetDefaultFontManager(ptr: Long, fontManagerPtr: Long, defaultFamilyName: String?): Long
        external fun _nGetFallbackManager(ptr: Long): Long
        external fun _nFindTypefaces(ptr: Long, familyNames: Array<String?>?, fontStyle: Int): LongArray
        external fun _nDefaultFallbackChar(ptr: Long, unicode: Int, fontStyle: Int, locale: String?): Long
        external fun _nDefaultFallback(ptr: Long): Long
        external fun _nSetEnableFallback(ptr: Long, value: Boolean): Long
        external fun _nGetParagraphCache(ptr: Long): Long

        init {
            staticLoad()
        }
    }

    constructor() : this(_nMake()) {
        Stats.onNativeCall()
    }

    val fontManagersCount: Long
        get() = try {
            Stats.onNativeCall()
            _nGetFontManagersCount(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setAssetFontManager(fontMgr: FontMgr?): FontCollection {
        return try {
            Stats.onNativeCall()
            _nSetAssetFontManager(_ptr, Native.Companion.getPtr(fontMgr))
            this
        } finally {
            Reference.reachabilityFence(fontMgr)
        }
    }

    fun setDynamicFontManager(fontMgr: FontMgr?): FontCollection {
        return try {
            Stats.onNativeCall()
            _nSetDynamicFontManager(
                _ptr,
                Native.Companion.getPtr(fontMgr)
            )
            this
        } finally {
            Reference.reachabilityFence(fontMgr)
        }
    }

    fun setTestFontManager(fontMgr: FontMgr?): FontCollection {
        return try {
            Stats.onNativeCall()
            _nSetTestFontManager(_ptr, Native.Companion.getPtr(fontMgr))
            this
        } finally {
            Reference.reachabilityFence(fontMgr)
        }
    }

    fun setDefaultFontManager(fontMgr: FontMgr?): FontCollection {
        return setDefaultFontManager(fontMgr, null)
    }

    fun setDefaultFontManager(fontMgr: FontMgr?, defaultFamilyName: String?): FontCollection {
        return try {
            Stats.onNativeCall()
            _nSetDefaultFontManager(
                _ptr,
                Native.Companion.getPtr(fontMgr),
                defaultFamilyName
            )
            this
        } finally {
            Reference.reachabilityFence(fontMgr)
        }
    }

    val fallbackManager: FontMgr?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetFallbackManager(_ptr)
            if (ptr == 0L) null else FontMgr(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun findTypefaces(familyNames: Array<String?>?, style: FontStyle): Array<Typeface?> {
        return try {
            Stats.onNativeCall()
            val ptrs = _nFindTypefaces(_ptr, familyNames, style._value)
            val res = arrayOfNulls<Typeface>(ptrs.size)
            for (i in ptrs.indices) res[i] = Typeface(ptrs[i])
            res
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun defaultFallback(unicode: Int, style: FontStyle, locale: String?): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = _nDefaultFallbackChar(_ptr, unicode, style._value, locale)
            if (ptr == 0L) null else Typeface(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun defaultFallback(): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = _nDefaultFallback(_ptr)
            if (ptr == 0L) null else Typeface(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun setEnableFallback(value: Boolean): FontCollection {
        Stats.onNativeCall()
        _nSetEnableFallback(_ptr, value)
        return this
    }

    val paragraphCache: org.jetbrains.skija.paragraph.ParagraphCache
        get() = try {
            Stats.onNativeCall()
            ParagraphCache(this, _nGetParagraphCache(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }
}