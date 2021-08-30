package org.jetbrains.skija.shaper

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
import java.lang.UnsupportedOperationException
import java.lang.ref.Reference
import java.text.Bidi
import java.util.*

/**
 * Shapes text using HarfBuzz and places the shaped text into a
 * client-managed buffer.
 */
class Shaper @ApiStatus.Internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @Contract("-> new")
        fun makePrimitive(): Shaper {
            Stats.onNativeCall()
            return Shaper(_nMakePrimitive())
        }

        @Contract("-> new")
        fun makeShaperDrivenWrapper(): Shaper {
            return makeShaperDrivenWrapper(null)
        }

        @Contract("_ -> new")
        fun makeShaperDrivenWrapper(fontMgr: FontMgr?): Shaper {
            return try {
                Stats.onNativeCall()
                Shaper(_nMakeShaperDrivenWrapper(Native.Companion.getPtr(fontMgr)))
            } finally {
                Reference.reachabilityFence(fontMgr)
            }
        }

        @Contract("-> new")
        fun makeShapeThenWrap(): Shaper {
            return makeShapeThenWrap(null)
        }

        @Contract("_ -> new")
        fun makeShapeThenWrap(fontMgr: FontMgr?): Shaper {
            return try {
                Stats.onNativeCall()
                Shaper(_nMakeShapeThenWrap(Native.Companion.getPtr(fontMgr)))
            } finally {
                Reference.reachabilityFence(fontMgr)
            }
        }

        @Contract("-> new")
        fun makeShapeDontWrapOrReorder(): Shaper {
            return makeShapeDontWrapOrReorder(null)
        }

        @Contract("_ -> new")
        fun makeShapeDontWrapOrReorder(fontMgr: FontMgr?): Shaper {
            return try {
                Stats.onNativeCall()
                Shaper(_nMakeShapeDontWrapOrReorder(Native.Companion.getPtr(fontMgr)))
            } finally {
                Reference.reachabilityFence(fontMgr)
            }
        }

        /**
         *
         * Only works on macOS
         *
         *
         * WARN broken in m87 https://bugs.chromium.org/p/skia/issues/detail?id=10897
         *
         * @return  Shaper on macOS, throws UnsupportedOperationException elsewhere
         */
        @Contract("-> new")
        fun makeCoreText(): Shaper {
            Stats.onNativeCall()
            val ptr = _nMakeCoreText()
            if (ptr == 0L) throw UnsupportedOperationException("CoreText not available")
            return Shaper(ptr)
        }

        @Contract("-> new")
        fun make(): Shaper {
            return make(null)
        }

        @Contract("_ -> new")
        fun make(fontMgr: FontMgr?): Shaper {
            return try {
                Stats.onNativeCall()
                Shaper(_nMake(Native.Companion.getPtr(fontMgr)))
            } finally {
                Reference.reachabilityFence(fontMgr)
            }
        }

        external fun _nGetFinalizer(): Long
        external fun _nMakePrimitive(): Long
        external fun _nMakeShaperDrivenWrapper(fontMgrPtr: Long): Long
        external fun _nMakeShapeThenWrap(fontMgrPtr: Long): Long
        external fun _nMakeShapeDontWrapOrReorder(fontMgrPtr: Long): Long
        external fun _nMakeCoreText(): Long
        external fun _nMake(fontMgrPtr: Long): Long
        external fun _nShapeBlob(
            ptr: Long,
            text: String?,
            fontPtr: Long,
            opts: ShapingOptions?,
            width: Float,
            offsetX: Float,
            offsetY: Float
        ): Long

        external fun _nShapeLine(ptr: Long, text: String?, fontPtr: Long, opts: ShapingOptions?): Long
        external fun _nShape(
            ptr: Long,
            textPtr: Long,
            fontIter: Iterator<FontRun?>?,
            bidiIter: Iterator<BidiRun?>?,
            scriptIter: Iterator<ScriptRun?>?,
            langIter: Iterator<LanguageRun?>?,
            opts: ShapingOptions?,
            width: Float,
            runHandler: RunHandler?
        )

        init {
            staticLoad()
        }
    }

    @Contract("_, _ -> new")
    fun shape(text: String?, font: Font?): TextBlob? {
        return shape(text, font, ShapingOptions.DEFAULT, Float.POSITIVE_INFINITY, Point.Companion.ZERO)
    }

    @Contract("_, _, _ -> new")
    fun shape(text: String?, font: Font?, width: Float): TextBlob? {
        return shape(text, font, ShapingOptions.DEFAULT, width, Point.Companion.ZERO)
    }

    @Contract("_, _, _, _ -> new")
    fun shape(text: String?, font: Font?, width: Float, offset: Point): TextBlob? {
        return shape(text, font, ShapingOptions.DEFAULT, width, offset)
    }

    @Contract("_, _, _, _, _ -> new")
    fun shape(text: String?, font: Font?, opts: ShapingOptions, width: Float, offset: Point): TextBlob? {
        return try {
            assert(opts != null) { "Can’t Shaper::shape with opts == null" }
            Stats.onNativeCall()
            val ptr = _nShapeBlob(
                _ptr,
                text,
                Native.Companion.getPtr(font),
                opts,
                width,
                offset._x,
                offset._y
            )
            if (0L == ptr) null else TextBlob(ptr)
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(font)
        }
    }

    @Contract("_, _, _, _, _ -> this")
    fun shape(
        text: String,
        font: Font?,
        opts: ShapingOptions,
        width: Float,
        runHandler: RunHandler
    ): Shaper {
        ManagedString(text).use { textUtf8 ->
            FontMgrRunIterator(textUtf8, false, font, opts).use { fontIter ->
                IcuBidiRunIterator(
                    textUtf8,
                    false,
                    if (opts._leftToRight) Bidi.DIRECTION_LEFT_TO_RIGHT else Bidi.DIRECTION_RIGHT_TO_LEFT
                ).use { bidiIter ->
                    HbIcuScriptRunIterator(textUtf8, false).use { scriptIter ->
                        val langIter: Iterator<LanguageRun> =
                            TrivialLanguageRunIterator(text, Locale.getDefault().toLanguageTag())
                        return shape(textUtf8, fontIter, bidiIter, scriptIter, langIter, opts, width, runHandler)
                    }
                }
            }
        }
    }

    @Contract("_, _, _, _, _, _, _ -> this")
    fun shape(
        text: String,
        fontIter: Iterator<FontRun?>,
        bidiIter: Iterator<BidiRun?>,
        scriptIter: Iterator<ScriptRun?>,
        langIter: Iterator<LanguageRun?>,
        opts: ShapingOptions,
        width: Float,
        runHandler: RunHandler
    ): Shaper {
        ManagedString(text).use { textUtf8 ->
            return shape(
                textUtf8,
                fontIter,
                bidiIter,
                scriptIter,
                langIter,
                opts,
                width,
                runHandler
            )
        }
    }

    @Contract("_, _, _, _, _, _, _ -> this")
    fun shape(
        textUtf8: ManagedString,
        fontIter: Iterator<FontRun?>,
        bidiIter: Iterator<BidiRun?>,
        scriptIter: Iterator<ScriptRun?>,
        langIter: Iterator<LanguageRun?>,
        opts: ShapingOptions,
        width: Float,
        runHandler: RunHandler
    ): Shaper {
        assert(fontIter != null) { "FontRunIterator == null" }
        assert(bidiIter != null) { "BidiRunIterator == null" }
        assert(scriptIter != null) { "ScriptRunIterator == null" }
        assert(langIter != null) { "LanguageRunIterator == null" }
        assert(opts != null) { "Can’t Shaper::shape with opts == null" }
        Stats.onNativeCall()
        _nShape(
            _ptr,
            Native.Companion.getPtr(textUtf8),
            fontIter,
            bidiIter,
            scriptIter,
            langIter,
            opts,
            width,
            runHandler
        )
        return this
    }

    @Contract("_, _, _ -> new")
    fun shapeLine(text: String?, font: Font?, opts: ShapingOptions): TextLine {
        return try {
            assert(opts != null) { "Can’t Shaper::shapeLine with opts == null" }
            Stats.onNativeCall()
            org.jetbrains.skija.TextLine(
                _nShapeLine(
                    _ptr,
                    text,
                    Native.Companion.getPtr(font),
                    opts
                )
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(font)
        }
    }

    @Contract("_, _, _ -> new")
    fun shapeLine(text: String?, font: Font?): TextLine {
        return shapeLine(text, font, ShapingOptions.DEFAULT)
    }

    @ApiStatus.Internal
    object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}