package org.jetbrains.skia.shaper

import org.jetbrains.skia.*
import org.jetbrains.skia.FontFeature.Companion.arrayOfFontFeaturesToInterop
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import kotlin.jvm.JvmStatic

/**
 * Shapes text using HarfBuzz and places the shaped text into a
 * client-managed buffer.
 */
class Shaper internal constructor(ptr: NativePointer) : Managed(ptr, _FinalizerHolder.PTR) {
    companion object {
        @JvmStatic
        fun makePrimitive(): Shaper = nativeCallFromStatic { Shaper(_nMakePrimitive()) }

        @JvmStatic
        fun makeShaperDrivenWrapper(): Shaper = makeShaperDrivenWrapper(null)

        @JvmStatic
        fun makeShaperDrivenWrapper(fontMgr: FontMgr?): Shaper = nativeCallFromStatic(fontMgr) { fontMgr ->
            Shaper(_nMakeShaperDrivenWrapper(fontMgr.pointerOrNullPointer))
        }

        @JvmStatic
        fun makeShapeThenWrap(): Shaper {
            return makeShapeThenWrap(null)
        }

        @JvmStatic
        fun makeShapeThenWrap(fontMgr: FontMgr?): Shaper = nativeCallFromStatic(fontMgr) { fontMgr ->
            Shaper(_nMakeShapeThenWrap(fontMgr.pointerOrNullPointer))
        }

        @JvmStatic
        fun makeShapeDontWrapOrReorder(): Shaper {
            return makeShapeDontWrapOrReorder(null)
        }

        @JvmStatic
        fun makeShapeDontWrapOrReorder(fontMgr: FontMgr?): Shaper = nativeCallFromStatic(fontMgr) { fontMgr ->
            Shaper(_nMakeShapeDontWrapOrReorder(fontMgr.pointerOrNullPointer))
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

        @JvmStatic
        fun makeCoreText(): Shaper {
            Stats.onNativeCall()
            val ptr = _nMakeCoreText()
            if (ptr == NullPointer) throw UnsupportedOperationException("CoreText not available")
            return Shaper(ptr)
        }

        @JvmStatic
        fun make(): Shaper = make(null)

        @JvmStatic
        fun make(fontMgr: FontMgr?): Shaper = nativeCallFromStatic(fontMgr) { fontMgr ->
            Shaper(Shaper_nMake(fontMgr.pointerOrNullPointer))
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
        return nativeCall(font) { font ->
            val ptr = ManagedString(text).use { managedString ->
                interopScope {
                    _nShapeBlob(
                        _ptr,
                        managedString._ptr,
                        font.pointerOrNullPointer,
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
    ): Shaper = nativeCall(textUtf8) { textUtf8 ->
        doShape(textUtf8, fontIter, bidiIter, scriptIter, langIter, opts, width, runHandler)
        this
    }

    fun shapeLine(text: String?, font: Font?, opts: ShapingOptions): TextLine {
        return nativeCall(font) { font ->
            ManagedString(text).use { managedString ->
                interopScope {
                    val linePtr = _nShapeLine(
                        _ptr,
                        managedString._ptr,
                        font.pointerOrNullPointer,
                        optsFeaturesLen = opts.features?.size ?: 0,
                        optsFeatures = arrayOfFontFeaturesToInterop(opts.features),
                        optsBooleanProps = opts._booleanPropsToInt()
                    )
                    require(linePtr != NullPointer) { "Shape line: $text returned nullptr" }
                    TextLine(linePtr)
                }
            }
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

// <editor-fold defaultstate="collapsed" desc="Native External Functions">
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nGetFinalizer")
// No ForceNativeThreadState: only returns the address of a delete function, so it is short and cannot block.
// Native call path: deleteShaper
private external fun Shaper_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMake")
// Needs ForceNativeThreadState: a first call can wait for another thread that is still initializing ICU.
// Native call path: SkUnicodes::ICU::Make -> SkGetICULib
@ForceNativeThreadState
private external fun Shaper_nMake(fontMgrPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakePrimitive")
// No ForceNativeThreadState: only allocates an empty primitive shaper object, so it is short and cannot block.
// Native call path: PrimitiveText -> make_unique<SkShaperPrimitive>
private external fun _nMakePrimitive(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeShaperDrivenWrapper")
// Needs ForceNativeThreadState: a first call can wait for another thread that is still initializing ICU.
// Native call path: SkUnicodes::ICU::Make -> SkGetICULib
@ForceNativeThreadState
private external fun _nMakeShaperDrivenWrapper(fontMgrPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeShapeThenWrap")
// Needs ForceNativeThreadState: a first call can wait for another thread that is still initializing ICU.
// Native call path: SkUnicodes::ICU::Make -> SkGetICULib
@ForceNativeThreadState
private external fun _nMakeShapeThenWrap(fontMgrPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeShapeDontWrapOrReorder")
// Needs ForceNativeThreadState: a first call can wait for another thread that is still initializing ICU.
// Native call path: SkUnicodes::ICU::Make -> SkGetICULib
@ForceNativeThreadState
private external fun _nMakeShapeDontWrapOrReorder(fontMgrPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nMakeCoreText")
// No ForceNativeThreadState: only stores the line-break mode, and CoreText work happens later in shape, so it is short
// and cannot block.
// Native call path: SkShaper_CoreText::SkShaper_CoreText
private external fun _nMakeCoreText(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nShapeBlob")
// Needs ForceNativeThreadState: shaping reads font tables through CoreText, a platform API, and the result is built
// into a blob.
// Native call path: SkShaper::shape -> skhb_get_table -> SkTypeface_Mac::onCopyTableData -> CTFontCopyTable
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


@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nShapeLine")
// Needs ForceNativeThreadState: shaping reads font tables through CoreText, a platform API, and also asks the font for
// metrics.
// Native call path: SkShaper::shape -> skhb_get_table -> SkTypeface_Mac::onCopyTableData -> CTFontCopyTable
@ForceNativeThreadState
private external fun _nShapeLine(
    ptr: NativePointer,
    text: NativePointer,
    fontPtr: NativePointer,
    optsFeaturesLen: Int,
    optsFeatures: InteropPointer,
    optsBooleanProps: Int
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper__1nShape")
// Needs ForceNativeThreadState: reads font tables through CoreText and calls Kotlin callbacks for text-run iterators
// and the result handler.
// Native call path: SkShaper::shape -> skhb_get_table -> copyTableData -> CTFontCopyTable
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
@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunIterator_1nGetFinalizer")
// No ForceNativeThreadState: only returns the address of a delete function, so it is short and cannot block.
// Native call path: deleteRunIterator
internal external fun RunIterator_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunIterator_1nCreateRunIterator")
// Needs ForceNativeThreadState: an unsupported type reaches a TODO path that prints and flushes stdout, which can block
// on stdout I/O.
// Native call path: TODO -> printf -> fflush
@ForceNativeThreadState
internal external fun RunIterator_nCreateRunIterator(type: Int, textPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunIterator_1nInitRunIterator")
// Needs ForceNativeThreadState: reinitializing replaces the old callback, whose disposal calls back into Kotlin.
// Native call path: SkikoRunIterator::init -> KCallback::~KCallback -> disposeCallback
@ForceNativeThreadState
internal external fun RunIterator_nInitRunIterator(
    ptr: NativePointer,
    type: Int,
    onConsume: InteropPointer,
    onEndOfCurrentRun: InteropPointer,
    onAtEnd: InteropPointer,
    onCurrent: InteropPointer
)

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nCreate")
// No ForceNativeThreadState: creates a handler with empty callbacks and empty vectors, so it is short and cannot block.
// Native call path: SkikoRunHandler::SkikoRunHandler
internal external fun RunHandler_nCreate(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetFinalizer")
// No ForceNativeThreadState: only returns the address of a delete function, so it is short and cannot block.
// Native call path: deleteRunHandler
internal external fun RunHandler_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nInit")
// Needs ForceNativeThreadState: reinitializing replaces the old callback, whose disposal calls back into Kotlin.
// Native call path: SkikoRunHandler::init -> KCallback::~KCallback -> disposeCallback
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

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetGlyphs")
// Needs ForceNativeThreadState: copies the whole run vector, so its time grows with the glyph count of the run.
// Native call path: std::copy
@ForceNativeThreadState
internal external fun RunHandler_nGetGlyphs(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetClusters")
// Needs ForceNativeThreadState: copies the whole run vector, so its time grows with the glyph count of the run.
// Native call path: std::copy
@ForceNativeThreadState
internal external fun RunHandler_nGetClusters(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetPositions")
// Needs ForceNativeThreadState: copies the whole run vector, so its time grows with the glyph count of the run.
// Native call path: std::copy
@ForceNativeThreadState
internal external fun RunHandler_nGetPositions(ptr: NativePointer, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nSetOffset")
// No ForceNativeThreadState: only stores a point as the offset, so it is short and cannot block.
// Native call path: SkikoRunHandler::setOffset
internal external fun RunHandler_nSetOffset(ptr: NativePointer, x: Float, y: Float)

@ExternalSymbolName("org_jetbrains_skia_shaper_Shaper_RunHandler_1nGetRunInfo")
// No ForceNativeThreadState: copies six stored run values and returns the address of the font, so it is short and
// cannot block.
// Native call path: SkikoRunHandler::runInfo
internal external fun RunHandler_nGetRunInfo(ptr: NativePointer, result: InteropPointer): NativePointer
// </editor-fold>