package org.jetbrains.skia.shaper

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
            ManagedRunIterator_nConsume(_ptr)
            ScriptRun(_getEndOfCurrentRun(), HbIcuScriptRunIterator_nGetCurrentScriptTag(_ptr))
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
        HbIcuScriptRunIterator_nMake(getPtr(text))
    } finally {
        reachabilityBarrier(text)
    }
}