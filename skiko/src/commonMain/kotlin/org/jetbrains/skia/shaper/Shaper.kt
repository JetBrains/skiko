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
                    val linePtr = _nShapeLine(
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


// Runnable: returns the address of deleteShaper only
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nGetFinalizer")
private external fun Shaper_nGetFinalizer(): NativePointer

// Native: SkUnicodes::ICU::Make -> SkGetICULib can wait for concurrent gICU initialization
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMake")
@ForceNativeThreadState
private external fun Shaper_nMake(fontMgrPtr: NativePointer): NativePointer

// Runnable: PrimitiveText() only does make_unique<SkShaperPrimitive>()
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakePrimitive")
private external fun _nMakePrimitive(): NativePointer

// Native: SkUnicodes::ICU::Make -> SkGetICULib can wait for concurrent gICU initialization
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeShaperDrivenWrapper")
@ForceNativeThreadState
private external fun _nMakeShaperDrivenWrapper(fontMgrPtr: NativePointer): NativePointer

// Native: SkUnicodes::ICU::Make -> SkGetICULib can wait for concurrent gICU initialization
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeShapeThenWrap")
@ForceNativeThreadState
private external fun _nMakeShapeThenWrap(fontMgrPtr: NativePointer): NativePointer

// Native: SkUnicodes::ICU::Make -> SkGetICULib can wait for concurrent gICU initialization
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeShapeDontWrapOrReorder")
@ForceNativeThreadState
private external fun _nMakeShapeDontWrapOrReorder(fontMgrPtr: NativePointer): NativePointer

// Runnable: SkShaper_CoreText ctor only stores the line-break mode
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeCoreText")
private external fun _nMakeCoreText(): NativePointer

// Native: SkShaper::shape -> skhb_get_table -> SkTypeface_Mac::onCopyTableData -> CTFontCopyTable
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nShapeBlob")
@ForceNativeThreadState
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


// Native: SkShaper::shape -> skhb_get_table -> onCopyTableData -> CTFontCopyTable; also font.getMetrics
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nShapeLine")
@ForceNativeThreadState
private external fun _nShapeLine(
    ptr: NativePointer,
    text: NativePointer,
    fontPtr: NativePointer,
    optsFeaturesLen: Int,
    optsFeatures: InteropPointer,
    optsBooleanProps: Int
): NativePointer

// Native: SkShaper::shape -> HB shaping -> copyTableData -> CTFontCopyTable and calls Kotlin run iterators/handler
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nShape")
@ForceNativeThreadState
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
// Runnable: returns the address of deleteRunIterator only
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunIterator_1nGetFinalizer")
internal external fun RunIterator_nGetFinalizer(): NativePointer

// Native: unsupported type -> TODO -> printf and fflush can block on stdout I/O
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunIterator_1nCreateRunIterator")
@ForceNativeThreadState
internal external fun RunIterator_nCreateRunIterator(type: Int, textPtr: NativePointer): NativePointer

// Native: SkikoRunIterator::init -> KCallback::~KCallback -> disposeCallback on reinitialization
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunIterator_1nInitRunIterator")
@ForceNativeThreadState
internal external fun RunIterator_nInitRunIterator(
    ptr: NativePointer,
    type: Int,
    onConsume: InteropPointer,
    onEndOfCurrentRun: InteropPointer,
    onAtEnd: InteropPointer,
    onCurrent: InteropPointer
)

// Runnable: new SkikoRunHandler holds KCallback(nullptr) members and empty vectors
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nCreate")
internal external fun RunHandler_nCreate(): NativePointer

// Runnable: returns the address of deleteRunHandler only
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetFinalizer")
internal external fun RunHandler_nGetFinalizer(): NativePointer

// Native: SkikoRunHandler::init -> KCallback::~KCallback -> disposeCallback on reinitialization
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nInit")
@ForceNativeThreadState
internal external fun RunHandler_nInit(
    ptr: NativePointer,
    onBeginLine: InteropPointer,
    onRunInfo: InteropPointer,
    onCommitRunInfo: InteropPointer,
    onRunOffset: InteropPointer,
    onCommitRun: InteropPointer,
    onCommitLine: InteropPointer
)

// Native: std::copy over the run vector, which grows with the glyph count of the run
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetGlyphs")
@ForceNativeThreadState
internal external fun RunHandler_nGetGlyphs(ptr: NativePointer, result: InteropPointer)

// Native: std::copy over the run vector, which grows with the glyph count of the run
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetClusters")
@ForceNativeThreadState
internal external fun RunHandler_nGetClusters(ptr: NativePointer, result: InteropPointer)

// Native: std::copy over the run vector, which grows with the glyph count of the run
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetPositions")
@ForceNativeThreadState
internal external fun RunHandler_nGetPositions(ptr: NativePointer, result: InteropPointer)

// Runnable: stores a SkPoint into _point
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nSetOffset")
internal external fun RunHandler_nSetOffset(ptr: NativePointer, x: Float, y: Float)

// Runnable: copies six RunInfo fields and returns the address of fFont
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetRunInfo")
internal external fun RunHandler_nGetRunInfo(ptr: NativePointer, result: InteropPointer): NativePointer
