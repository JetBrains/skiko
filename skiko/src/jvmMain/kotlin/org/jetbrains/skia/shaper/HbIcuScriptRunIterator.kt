package org.jetbrains.skia.shaper

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import java.lang.ref.Reference

class HbIcuScriptRunIterator(text: ManagedString?, manageText: Boolean) : ManagedRunIterator<ScriptRun?>(
    _nMake(
        Native.getPtr(text)
    ), text, manageText
) {
    companion object {
        @JvmStatic external fun _nMake(textPtr: Long): Long
        @JvmStatic external fun _nGetCurrentScriptTag(ptr: Long): Int

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
            Reference.reachabilityFence(this)
        }
    }

    init {
        Stats.onNativeCall()
        Reference.reachabilityFence(text)
    }

    override fun remove() {
        TODO("Not yet implemented")
    }
}