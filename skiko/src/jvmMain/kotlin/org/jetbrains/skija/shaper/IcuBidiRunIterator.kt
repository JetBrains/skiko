package org.jetbrains.skija.shaper

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.ManagedString
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class IcuBidiRunIterator(text: ManagedString?, manageText: Boolean, bidiLevel: Int) : ManagedRunIterator<BidiRun?>(
    _nMake(Native.Companion.getPtr(text), bidiLevel), text, manageText
) {
    companion object {
        @ApiStatus.Internal
        external fun _nMake(textPtr: Long, bidiLevel: Int): Long
        @ApiStatus.Internal
        external fun _nGetCurrentLevel(ptr: Long): Int

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