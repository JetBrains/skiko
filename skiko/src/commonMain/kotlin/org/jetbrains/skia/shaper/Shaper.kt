@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr

/**
 * Shapes text using HarfBuzz and places the shaped text into a
 * client-managed buffer.
 */
class Shaper internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
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
            if (NullPointer == ptr) null else TextBlob(ptr)
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
        val PTR = Shaper_nGetFinalizer()
    }
}


@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nGetFinalizer")
private external fun Shaper_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMake")
private external fun Shaper_nMake(fontMgrPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakePrimitive")
private external fun _nMakePrimitive(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeShaperDrivenWrapper")
private external fun _nMakeShaperDrivenWrapper(fontMgrPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeShapeThenWrap")
private external fun _nMakeShapeThenWrap(fontMgrPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeShapeDontWrapOrReorder")
private external fun _nMakeShapeDontWrapOrReorder(fontMgrPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeCoreText")
private external fun _nMakeCoreText(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nShapeBlob")
private external fun _nShapeBlob(
    ptr: NativePointer,
    text: String?,
    fontPtr: NativePointer,
    opts: ShapingOptions?,
    width: Float,
    offsetX: Float,
    offsetY: Float
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nShapeLine")
private external fun _nShapeLine(ptr: NativePointer, text: String?, fontPtr: NativePointer, opts: ShapingOptions?): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nShape")
private external fun _nShape(
    ptr: NativePointer,
    textPtr: NativePointer,
    fontIter: Iterator<FontRun?>?,
    bidiIter: Iterator<BidiRun?>?,
    scriptIter: Iterator<ScriptRun?>?,
    langIter: Iterator<LanguageRun?>?,
    opts: ShapingOptions?,
    width: Float,
    runHandler: RunHandler?
)
