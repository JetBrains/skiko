@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import kotlin.jvm.JvmStatic

class HbIcuScriptRunIterator(text: ManagedString?, manageText: Boolean) : ManagedRunIterator<ScriptRun?>(
    _nMake(
        getPtr(text)
    ), text, manageText
) {
    companion object {
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_HbIcuScriptRunIterator__1nMake")
        external fun _nMake(textPtr: Long): Long
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_HbIcuScriptRunIterator__1nGetCurrentScriptTag")
        external fun _nGetCurrentScriptTag(ptr: Long): Int

        init {
            staticLoad()
        }
    }

    constructor(text: String?) : this(ManagedString(text), true) {}

    override operator fun next(): ScriptRun {
        return try {
            _nConsume(_ptr)
            ScriptRun(_getEndOfCurrentRun(), _nGetCurrentScriptTag(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
    }

    init {
        Stats.onNativeCall()
        reachabilityBarrier(text)
    }

    override fun remove() {
        TODO("Not yet implemented")
    }
}