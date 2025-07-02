@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.Font
import org.jetbrains.skia.FontFeature.Companion.arrayOfFontFeaturesToInterop
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.Point
import org.jetbrains.skia.TextBlob
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.defaultLanguageTag
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.use

/**
 * Shapes text using HarfBuzz and places the shaped text into a
 * client-managed buffer.
 */
class Shaper internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        fun makePrimitive(): Shaper {
            Stats.onNativeCall()
            return Shaper(Shaper_nMakePrimitive())
        }

        fun makeShaperDrivenWrapper(): Shaper {
            return makeShaperDrivenWrapper(null)
        }

        fun makeShaperDrivenWrapper(fontMgr: FontMgr?): Shaper {
            return try {
                Stats.onNativeCall()
                Shaper(Shaper_nMakeShaperDrivenWrapper(getPtr(fontMgr)))
            } finally {
                reachabilityBarrier(fontMgr)
            }
        }

        fun makeShapeThenWrap(): Shaper {
            return makeShapeThenWrap(null)
        }

        fun makeShapeThenWrap(fontMgr: FontMgr?): Shaper {
            return try {
                Stats.onNativeCall()
                Shaper(Shaper_nMakeShapeThenWrap(getPtr(fontMgr)))
            } finally {
                reachabilityBarrier(fontMgr)
            }
        }

        fun makeShapeDontWrapOrReorder(): Shaper {
            return makeShapeDontWrapOrReorder(null)
        }

        fun makeShapeDontWrapOrReorder(fontMgr: FontMgr?): Shaper {
            return try {
                Stats.onNativeCall()
                Shaper(Shaper_nMakeShapeDontWrapOrReorder(getPtr(fontMgr)))
            } finally {
                reachabilityBarrier(fontMgr)
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
        fun makeCoreText(): Shaper {
            Stats.onNativeCall()
            val ptr = Shaper_nMakeCoreText()
            if (ptr == NullPointer) throw UnsupportedOperationException("CoreText not available")
            return Shaper(ptr)
        }

        fun make(): Shaper {
            return make(null)
        }

        fun make(fontMgr: FontMgr?): Shaper {
            return try {
                Stats.onNativeCall()
                Shaper(Shaper_nMake(getPtr(fontMgr)))
            } finally {
                reachabilityBarrier(fontMgr)
            }
        }

        init {
            staticLoad()
        }
    }

    fun shape(text: String, font: Font): TextBlob? {
        return shape(text, font, ShapingOptions.DEFAULT, Float.POSITIVE_INFINITY, Point.Companion.ZERO)
    }

    fun shape(text: String, font: Font, width: Float): TextBlob? {
        return shape(text, font, ShapingOptions.DEFAULT, width, Point.Companion.ZERO)
    }

    fun shape(text: String, font: Font, width: Float, offset: Point): TextBlob? {
        return shape(text, font, ShapingOptions.DEFAULT, width, offset)
    }

    fun shape(text: String, font: Font, opts: ShapingOptions, width: Float, offset: Point): TextBlob? {
        return try {
            Stats.onNativeCall()
            val ptr = ManagedString(text).use { managedString ->
                interopScope {
                    Shaper_nShapeBlob(
                        _ptr,
                        managedString._ptr,
                        getPtr(font),
                        optsFeaturesLen = opts.features?.size ?: 0,
                        optsFeaturesIntArray = arrayOfFontFeaturesToInterop(opts.features),
                        optsBooleanProps = opts._booleanPropsToInt(),
                        width = width,
                        offsetX = offset.x,
                        offsetY = offset.y
                    )
                }
            }
            if (NullPointer == ptr) null else TextBlob(ptr)
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(font)
        }
    }

    fun shape(
        text: String,
        font: Font,
        opts: ShapingOptions,
        width: Float,
        runHandler: RunHandler
    ): Shaper {
        ManagedString(text).use { textUtf8 ->
            FontMgrRunIterator(textUtf8, false, font, opts).use { fontIter ->
                IcuBidiRunIterator(
                    textUtf8,
                    false,
                    if (opts.isLeftToRight) -2 /* Bidi.DIRECTION_LEFT_TO_RIGHT */ else -1 /* Bidi.DIRECTION_RIGHT_TO_LEFT */
                ).use { bidiIter ->
                    HbIcuScriptRunIterator(textUtf8, false).use { scriptIter ->
                        val langIter =
                            TrivialLanguageRunIterator(text, defaultLanguageTag())
                        return shape(textUtf8, fontIter, bidiIter, scriptIter, langIter, opts, width, runHandler)
                    }
                }
            }
        }
    }

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
        Stats.onNativeCall()
        doShape(textUtf8, fontIter, bidiIter, scriptIter, langIter, opts, width, runHandler)
        return this
    }

    fun shapeLine(text: String?, font: Font?, opts: ShapingOptions): TextLine {
        return try {
            Stats.onNativeCall()
            ManagedString(text).use { managedString ->
                interopScope {
                    val linePtr = Shaper_nShapeLine(
                        _ptr,
                        managedString._ptr,
                        getPtr(font),
                        optsFeaturesLen = opts.features?.size ?: 0,
                        optsFeatures = arrayOfFontFeaturesToInterop(opts.features),
                        optsBooleanProps = opts._booleanPropsToInt()
                    )
                    require(linePtr != NullPointer) { "Shape line: $text returned nullptr" }
                    TextLine(linePtr)
                }

            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(font)
        }
    }

    fun shapeLine(text: String?, font: Font?): TextLine {
        return shapeLine(text, font, ShapingOptions.DEFAULT)
    }

    private object _FinalizerHolder {
        val PTR = Shaper_nGetFinalizer()
    }
}

internal expect fun Shaper.doShape(
    textUtf8: ManagedString,
    fontIter: Iterator<FontRun?>,
    bidiIter: Iterator<BidiRun?>,
    scriptIter: Iterator<ScriptRun?>,
    langIter: Iterator<LanguageRun?>,
    opts: ShapingOptions,
    width: Float,
    runHandler: RunHandler
)