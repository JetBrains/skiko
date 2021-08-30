package org.jetbrains.skija.skottie

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
import org.jetbrains.skija.impl.Managed
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class AnimationBuilder @ApiStatus.Internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @ApiStatus.Internal
        fun _flagsToInt(vararg builderFlags: AnimationBuilderFlag): Int {
            var flags = 0
            for (flag in builderFlags) flags = flags or flag._flag
            return flags
        }

        @ApiStatus.Internal
        external fun _nGetFinalizer(): Long
        @ApiStatus.Internal
        external fun _nMake(flags: Int): Long
        @ApiStatus.Internal
        external fun _nSetFontManager(ptr: Long, fontMgrPtr: Long)
        @ApiStatus.Internal
        external fun _nSetLogger(ptr: Long, loggerPtr: Long)
        @ApiStatus.Internal
        external fun _nBuildFromString(ptr: Long, data: String?): Long
        @ApiStatus.Internal
        external fun _nBuildFromFile(ptr: Long, path: String?): Long
        @ApiStatus.Internal
        external fun _nBuildFromData(ptr: Long, dataPtr: Long): Long

        init {
            staticLoad()
        }
    }

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }

    constructor() : this(*arrayOfNulls<AnimationBuilderFlag>(0)) {}
    constructor(vararg builderFlags: AnimationBuilderFlag?) : this(_nMake(_flagsToInt(*builderFlags))) {
        Stats.onNativeCall()
    }

    /**
     *
     * Specify a font manager for loading animation fonts.
     */
    @Contract("_ -> this")
    fun setFontManager(fontMgr: FontMgr?): AnimationBuilder {
        return try {
            Stats.onNativeCall()
            _nSetFontManager(_ptr, Native.Companion.getPtr(fontMgr))
            this
        } finally {
            Reference.reachabilityFence(fontMgr)
        }
    }

    /**
     *
     * Register a [Logger] with this builder.
     */
    @Contract("_ -> this")
    fun setLogger(logger: Logger?): AnimationBuilder {
        return try {
            Stats.onNativeCall()
            _nSetLogger(_ptr, Native.Companion.getPtr(logger))
            this
        } finally {
            Reference.reachabilityFence(logger)
        }
    }

    @Contract("!null -> new; null -> fail")
    fun buildFromString(data: String): Animation {
        return try {
            assert(data != null) { "Can’t buildFromString with data == null" }
            Stats.onNativeCall()
            val ptr = _nBuildFromString(_ptr, data)
            require(ptr != 0L) { "Failed to create Animation from string: \"$data\"" }
            Animation(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    @Contract("!null -> new; null -> fail")
    fun buildFromFile(path: String): Animation {
        return try {
            assert(path != null) { "Can’t buildFromFile with path == null" }
            Stats.onNativeCall()
            val ptr = _nBuildFromFile(_ptr, path)
            require(ptr != 0L) { "Failed to create Animation from path: $path" }
            Animation(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    @Contract("!null -> new; null -> fail")
    fun buildFromData(data: Data): Animation {
        return try {
            assert(data != null) { "Can’t buildFromData with data == null" }
            Stats.onNativeCall()
            val ptr =
                _nBuildFromData(_ptr, Native.Companion.getPtr(data))
            require(ptr != 0L) { "Failed to create Animation from data" }
            Animation(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }
}