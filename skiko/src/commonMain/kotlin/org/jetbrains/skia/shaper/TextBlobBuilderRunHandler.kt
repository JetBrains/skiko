@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr

class TextBlobBuilderRunHandler<T> internal constructor(
    text: ManagedString?,
    manageText: Boolean,
    offsetX: Float,
    offsetY: Float
) : Managed(
    TextBlobBuilderRunHandler_nMake(getPtr(text), offsetX, offsetY), _FinalizerHolder.PTR
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

    fun makeBlob(): TextBlob? {
        return try {
            Stats.onNativeCall()
            val ptr = _nMakeBlob(_ptr)
            if (NullPointer == ptr) null else TextBlob(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    internal object _FinalizerHolder {
        val PTR = TextBlobBuilderRunHandler_nGetFinalizer()
    }

    init {
        // TODO [pavel.sergeev] what `manageText` == false is actually mean?
        _text = if (manageText) text else null
        reachabilityBarrier(text)
    }
}

@ExternalSymbolName("org_jetbrains_skia_TextBlobBuilderRunHandler__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextBlobBuilderRunHandler__1nGetFinalizer")
private external fun TextBlobBuilderRunHandler_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlobBuilderRunHandler__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextBlobBuilderRunHandler__1nMake")
private external fun TextBlobBuilderRunHandler_nMake(textPtr: NativePointer, offsetX: Float, offsetY: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_TextBlobBuilderRunHandler__1nMakeBlob")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_TextBlobBuilderRunHandler__1nMakeBlob")
private external fun _nMakeBlob(ptr: NativePointer): NativePointer
