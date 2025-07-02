@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

private fun makeIcuBidiRunIterator(text: ManagedString, bidiLevel: Int): NativePointer {
    Stats.onNativeCall()
    return try {
        IcuBidiRunIterator_nMake(getPtr(text), bidiLevel)
    } finally {
        reachabilityBarrier(text)
    }
}


class IcuBidiRunIterator(text: ManagedString, manageText: Boolean, bidiLevel: Int) : ManagedRunIterator<BidiRun?>(
    makeIcuBidiRunIterator(text, bidiLevel), text, manageText
) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor(text: String, bidiLevel: Int) : this(ManagedString(text), true, bidiLevel) {}

    override operator fun next(): BidiRun {
        return try {
            ManagedRunIterator_nConsume(_ptr)
            BidiRun(_getEndOfCurrentRun(), IcuBidiRunIterator_nGetCurrentLevel(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
    }

    override fun remove() {
        TODO("Not yet implemented")
    }
}