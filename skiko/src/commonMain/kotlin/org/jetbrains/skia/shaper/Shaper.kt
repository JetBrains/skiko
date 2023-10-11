@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.*
import org.jetbrains.skia.FontFeature.Companion.arrayOfFontFeaturesToInterop
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

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
                    _nShapeBlob(
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
                    TextLine(
                        _nShapeLine(
                            _ptr,
                            managedString._ptr,
                            getPtr(font),
                            optsFeaturesLen = opts.features?.size ?: 0,
                            optsFeatures = arrayOfFontFeaturesToInterop(opts.features),
                            optsBooleanProps = opts._booleanPropsToInt()
                        )
                    )
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


@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper__1nGetFinalizer")
private external fun Shaper_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper__1nMake")
private external fun Shaper_nMake(fontMgrPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakePrimitive")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper__1nMakePrimitive")
private external fun _nMakePrimitive(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeShaperDrivenWrapper")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper__1nMakeShaperDrivenWrapper")
private external fun _nMakeShaperDrivenWrapper(fontMgrPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeShapeThenWrap")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper__1nMakeShapeThenWrap")
private external fun _nMakeShapeThenWrap(fontMgrPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeShapeDontWrapOrReorder")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper__1nMakeShapeDontWrapOrReorder")
private external fun _nMakeShapeDontWrapOrReorder(fontMgrPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeCoreText")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper__1nMakeCoreText")
private external fun _nMakeCoreText(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nShapeBlob")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper__1nShapeBlob")
private external fun _nShapeBlob(
    ptr: NativePointer,
    text: NativePointer,
    fontPtr: NativePointer,
    optsFeaturesLen: Int,
    optsFeaturesIntArray: InteropPointer,
    optsBooleanProps: Int,
    width: Float,
    offsetX: Float,
    offsetY: Float
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nShapeLine")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper__1nShapeLine")
private external fun _nShapeLine(
    ptr: NativePointer,
    text: NativePointer,
    fontPtr: NativePointer,
    optsFeaturesLen: Int,
    optsFeatures: InteropPointer,
    optsBooleanProps: Int
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nShape")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper__1nShape")
internal external fun Shaper_nShape(
    ptr: NativePointer,
    textPtr: NativePointer,
    fontIter: InteropPointer,
    bidiIter: InteropPointer,
    scriptIter: InteropPointer,
    langIter: InteropPointer,
    optsFeaturesLen: Int,
    optsFeaturesIntArray: InteropPointer,
    optsBooleanProps: Int,
    width: Float,
    runHandler: InteropPointer
)

// Native/JS only
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunIterator_1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper_RunIterator_1nGetFinalizer")
internal external fun RunIterator_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunIterator_1nCreateRunIterator")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper_RunIterator_1nCreateRunIterator")
internal external fun RunIterator_nCreateRunIterator(type: Int, textPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunIterator_1nInitRunIterator")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper_RunIterator_1nInitRunIterator")
internal external fun RunIterator_nInitRunIterator(
    ptr: NativePointer,
    type: Int,
    onConsume: InteropPointer,
    onEndOfCurrentRun: InteropPointer,
    onAtEnd: InteropPointer,
    onCurrent: InteropPointer
)

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nCreate")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper_RunHandler_1nCreate")
internal external fun RunHandler_nCreate(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetFinalizer")
internal external fun RunHandler_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nInit")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper_RunHandler_1nInit")
internal external fun RunHandler_nInit(
    ptr: NativePointer,
    onBeginLine: InteropPointer,
    onRunInfo: InteropPointer,
    onCommitRunInfo: InteropPointer,
    onRunOffset: InteropPointer,
    onCommitRun: InteropPointer,
    onCommitLine: InteropPointer
)

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetGlyphs")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetGlyphs")
internal external fun RunHandler_nGetGlyphs(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetClusters")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetClusters")
internal external fun RunHandler_nGetClusters(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetPositions")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetPositions")
internal external fun RunHandler_nGetPositions(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nSetOffset")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper_RunHandler_1nSetOffset")
internal external fun RunHandler_nSetOffset(ptr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetRunInfo")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetRunInfo")
internal external fun RunHandler_nGetRunInfo(ptr: NativePointer, result: InteropPointer): NativePointer
