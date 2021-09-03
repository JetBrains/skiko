@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import kotlin.jvm.JvmStatic

class IcuBidiRunIterator(text: ManagedString?, manageText: Boolean, bidiLevel: Int) : ManagedRunIterator<BidiRun?>(
    _nMake(getPtr(text), bidiLevel), text, manageText
) {
    companion object {
        @JvmStatic external fun _nMake(textPtr: Long, bidiLevel: Int): Long
        @JvmStatic external fun _nGetCurrentLevel(ptr: Long): Int

        init {
            staticLoad()
        }
    }

    constructor(text: String?, bidiLevel: Int) : this(ManagedString(text), true, bidiLevel) {}

    override operator fun next(): BidiRun {
        return try {
            _nConsume(_ptr)
            BidiRun(_getEndOfCurrentRun(), _nGetCurrentLevel(_ptr))
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