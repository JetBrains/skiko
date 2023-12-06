@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
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
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_HbIcuScriptRunIterator__1nMake")
private external fun _nMake(textPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_HbIcuScriptRunIterator__1nGetCurrentScriptTag")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_shaper_HbIcuScriptRunIterator__1nGetCurrentScriptTag")
private external fun _nGetCurrentScriptTag(ptr: NativePointer): Int
