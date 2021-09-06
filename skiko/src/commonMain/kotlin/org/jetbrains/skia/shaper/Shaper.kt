@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.ExternalSymbolName
import kotlin.jvm.JvmStatic

/**
 * Shapes text using HarfBuzz and places the shaped text into a
 * client-managed buffer.
 */
class Shaper internal constructor(ptr: Long) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        fun makePrimitive(): Shaper {
            Stats.onNativeCall()
            return Shaper(_nMakePrimitive())
        }

        fun makeShaperDrivenWrapper(): Shaper {
            return makeShaperDrivenWrapper(null)
        }

        fun makeShaperDrivenWrapper(fontMgr: FontMgr?): Shaper {
            return try {
                Stats.onNativeCall()
                Shaper(_nMakeShaperDrivenWrapper(getPtr(fontMgr)))
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
                Shaper(_nMakeShapeThenWrap(getPtr(fontMgr)))
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
                Shaper(_nMakeShapeDontWrapOrReorder(getPtr(fontMgr)))
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
            val ptr = _nMakeCoreText()
            if (ptr == 0L) throw UnsupportedOperationException("CoreText not available")
            return Shaper(ptr)
        }

        fun make(): Shaper {
            return make(null)
        }

        fun make(fontMgr: FontMgr?): Shaper {
            return try {
                Stats.onNativeCall()
                Shaper(_nMake(getPtr(fontMgr)))
            } finally {
                reachabilityBarrier(fontMgr)
            }
        }

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Shaper__1nGetFinalizer")
        external fun _nGetFinalizer(): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Shaper__1nMakePrimitive")
        external fun _nMakePrimitive(): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Shaper__1nMakeShaperDrivenWrapper")
        external fun _nMakeShaperDrivenWrapper(fontMgrPtr: Long): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Shaper__1nMakeShapeThenWrap")
        external fun _nMakeShapeThenWrap(fontMgrPtr: Long): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Shaper__1nMakeShapeDontWrapOrReorder")
        external fun _nMakeShapeDontWrapOrReorder(fontMgrPtr: Long): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Shaper__1nMakeCoreText")
        external fun _nMakeCoreText(): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Shaper__1nMake")
        external fun _nMake(fontMgrPtr: Long): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Shaper__1nShapeBlob")
        external fun _nShapeBlob(
            ptr: Long,
            text: String?,
            fontPtr: Long,
            opts: ShapingOptions?,
            width: Float,
            offsetX: Float,
            offsetY: Float
        ): Long

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Shaper__1nShapeLine")
        external fun _nShapeLine(ptr: Long, text: String?, fontPtr: Long, opts: ShapingOptions?): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_Shaper__1nShape")
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

    fun shape(text: String?, font: Font?): TextBlob? {
        return shape(text, font, ShapingOptions.DEFAULT, Float.POSITIVE_INFINITY, Point.Companion.ZERO)
    }

    fun shape(text: String?, font: Font?, width: Float): TextBlob? {
        return shape(text, font, ShapingOptions.DEFAULT, width, Point.Companion.ZERO)
    }

    fun shape(text: String?, font: Font?, width: Float, offset: Point): TextBlob? {
        return shape(text, font, ShapingOptions.DEFAULT, width, offset)
    }

    fun shape(text: String?, font: Font?, opts: ShapingOptions, width: Float, offset: Point): TextBlob? {
        return try {
            Stats.onNativeCall()
            val ptr = _nShapeBlob(
                _ptr,
                text,
                getPtr(font),
                opts,
                width,
                offset.x,
                offset.y
            )
            if (0L == ptr) null else TextBlob(ptr)
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(font)
        }
    }

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
        _nShape(
            _ptr,
            getPtr(textUtf8),
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

    fun shapeLine(text: String?, font: Font?, opts: ShapingOptions): TextLine {
        return try {
            Stats.onNativeCall()
            TextLine(
                _nShapeLine(
                    _ptr,
                    text,
                    getPtr(font),
                    opts
                )
            )
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(font)
        }
    }

    fun shapeLine(text: String?, font: Font?): TextLine {
        return shapeLine(text, font, ShapingOptions.DEFAULT)
    }

    private object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}