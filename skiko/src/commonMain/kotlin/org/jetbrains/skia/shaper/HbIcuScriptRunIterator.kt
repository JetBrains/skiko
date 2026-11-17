package org.jetbrains.skia.shaper

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

class HbIcuScriptRunIterator(text: ManagedString, manageText: Boolean) : ManagedRunIterator<ScriptRun?>(
    makeHbIcuScriptRunIterator(text), text, manageText
) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor(text: String?) : this(ManagedString(text), true)

    override operator fun next(): ScriptRun {
        return try {
            _nConsume(_ptr)
            ScriptRun(_getEndOfCurrentRun(), _nGetCurrentScriptTag(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
    }

    override fun remove() {
        TODO("Not yet implemented")
    }
}

private fun makeHbIcuScriptRunIterator(text: ManagedString): NativePointer {
    Stats.onNativeCall()
    return try {
        _nMake(getPtr(text))
    } finally {
        reachabilityBarrier(text)
    }
}

@ExternalSymbolName("org_jetbrains_skia_shaper_HbIcuScriptRunIterator__1nMake")
// No ForceNativeThreadState: only stores the text pointer and range, and scanning is deferred to consume, so it is
// short and cannot block.
// Native call path: MakeHbIcuScriptRunIterator
private external fun _nMake(textPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_HbIcuScriptRunIterator__1nGetCurrentScriptTag")
// No ForceNativeThreadState: only packs a stored script value into a tag, so it is short and cannot block.
// Native call path: SkUnicodeHbScriptRunIterator::currentScript
private external fun _nGetCurrentScriptTag(ptr: NativePointer): Int
