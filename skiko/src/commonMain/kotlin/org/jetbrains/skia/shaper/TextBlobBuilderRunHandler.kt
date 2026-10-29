package org.jetbrains.skia.shaper

import org.jetbrains.skia.*
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class TextBlobBuilderRunHandler internal constructor(
    text: ManagedString?,
    manageText: Boolean,
    offsetX: Float,
    offsetY: Float
) : Managed(
    nativeCallFromStatic(text) { text ->
        TextBlobBuilderRunHandler_nMake(text.pointerOrNullPointer, offsetX, offsetY)
    }, _FinalizerHolder.PTR
), RunHandler {
    companion object {
        init {
            staticLoad()
        }
    }

    internal val _text: ManagedString?

    constructor(text: String?) : this(ManagedString(text), true, 0f, 0f)
    constructor(text: String?, offset: Point) : this(ManagedString(text), true, offset.x, offset.y)

    override fun close() {
        super.close()
        _text?.close()
    }

    override fun beginLine() {
        throw UnsupportedOperationException("beginLine")
    }

    override fun runInfo(info: RunInfo?) {
        throw UnsupportedOperationException("runInfo")
    }

    override fun commitRunInfo() {
        throw UnsupportedOperationException("commitRunInfo")
    }

    override fun runOffset(info: RunInfo?): Point {
        throw UnsupportedOperationException("runOffset")
    }

    override fun commitRun(info: RunInfo?, glyphs: ShortArray?, positions: Array<Point?>?, clusters: IntArray?) {
        throw UnsupportedOperationException("commitRun")
    }

    override fun commitLine() {
        throw UnsupportedOperationException("commitLine")
    }

    fun makeBlob(): TextBlob? = nativeCall {
        val ptr = _nMakeBlob(_ptr)
        if (NullPointer == ptr) null else TextBlob(ptr)
    }

    internal object _FinalizerHolder {
        val PTR = TextBlobBuilderRunHandler_nGetFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_shaper_TextBlobBuilderRunHandler__1nGetFinalizer")
// No ForceNativeThreadState: only returns the address of a delete function, so it is short and cannot block.
// Native call path: deleteTextBlobBuilderRunHandler
private external fun TextBlobBuilderRunHandler_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_TextBlobBuilderRunHandler__1nMake")
// No ForceNativeThreadState: stores the text pointer and offset and starts an empty builder, so it is short and cannot
// block.
// Native call path: SkTextBlobBuilderRunHandler::SkTextBlobBuilderRunHandler
private external fun TextBlobBuilderRunHandler_nMake(textPtr: NativePointer, offsetX: Float, offsetY: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_TextBlobBuilderRunHandler__1nMakeBlob")
// Needs ForceNativeThreadState: computing the text's bounds obtains glyph measurements through the macOS font scaler.
// Native call path: SkTextBlobBuilder::make -> updateDeferredBounds -> TightRunBounds -> SkFont::getBounds
@ForceNativeThreadState
private external fun _nMakeBlob(ptr: NativePointer): NativePointer
