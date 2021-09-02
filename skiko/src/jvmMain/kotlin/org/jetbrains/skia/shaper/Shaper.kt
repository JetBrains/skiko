package org.jetbrains.skia.shaper

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import java.lang.UnsupportedOperationException
import java.lang.ref.Reference
import java.text.Bidi
import java.util.*

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
                Shaper(_nMakeShaperDrivenWrapper(Native.Companion.getPtr(fontMgr)))
            } finally {
                Reference.reachabilityFence(fontMgr)
            }
        }

        fun makeShapeThenWrap(): Shaper {
            return makeShapeThenWrap(null)
        }

        fun makeShapeThenWrap(fontMgr: FontMgr?): Shaper {
            return try {
                Stats.onNativeCall()
                Shaper(_nMakeShapeThenWrap(Native.Companion.getPtr(fontMgr)))
            } finally {
                Reference.reachabilityFence(fontMgr)
            }
        }

        fun makeShapeDontWrapOrReorder(): Shaper {
            return makeShapeDontWrapOrReorder(null)
        }

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
                Shaper(_nMake(Native.Companion.getPtr(fontMgr)))
            } finally {
                Reference.reachabilityFence(fontMgr)
            }
        }

        @JvmStatic
        external fun _nGetFinalizer(): Long
        @JvmStatic
        external fun _nMakePrimitive(): Long
        @JvmStatic
        external fun _nMakeShaperDrivenWrapper(fontMgrPtr: Long): Long
        @JvmStatic
        external fun _nMakeShapeThenWrap(fontMgrPtr: Long): Long
        @JvmStatic
        external fun _nMakeShapeDontWrapOrReorder(fontMgrPtr: Long): Long
        @JvmStatic
        external fun _nMakeCoreText(): Long
        @JvmStatic
        external fun _nMake(fontMgrPtr: Long): Long
        @JvmStatic
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
        external fun _nShapeLine(ptr: Long, text: String?, fontPtr: Long, opts: ShapingOptions?): Long
        @JvmStatic
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
            assert(opts != null) { "Can’t Shaper::shape with opts == null" }
            Stats.onNativeCall()
            val ptr = _nShapeBlob(
                _ptr,
                text,
                Native.Companion.getPtr(font),
                opts,
                width,
                offset.x,
                offset.y
            )
            if (0L == ptr) null else TextBlob(ptr)
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(font)
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
                    if (opts.isLeftToRight) Bidi.DIRECTION_LEFT_TO_RIGHT else Bidi.DIRECTION_RIGHT_TO_LEFT
                ).use { bidiIter ->
                    HbIcuScriptRunIterator(textUtf8, false).use { scriptIter ->
                        val langIter =
                            TrivialLanguageRunIterator(text, Locale.getDefault().toLanguageTag())
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

    fun shapeLine(text: String?, font: Font?, opts: ShapingOptions): TextLine {
        return try {
            assert(opts != null) { "Can’t Shaper::shapeLine with opts == null" }
            Stats.onNativeCall()
            org.jetbrains.skia.TextLine(
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

    fun shapeLine(text: String?, font: Font?): TextLine {
        return shapeLine(text, font, ShapingOptions.DEFAULT)
    }

    private object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}