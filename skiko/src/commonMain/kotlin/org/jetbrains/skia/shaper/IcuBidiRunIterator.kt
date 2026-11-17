package org.jetbrains.skia.shaper

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

private fun makeIcuBidiRunIterator(text: ManagedString, bidiLevel: Int): NativePointer {
    Stats.onNativeCall()
    return try {
        _nMake(getPtr(text), bidiLevel)
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
            _nConsume(_ptr)
            BidiRun(_getEndOfCurrentRun(), _nGetCurrentLevel(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
    }

    override fun remove() {
        TODO("Not yet implemented")
    }
}

@ExternalSymbolName("org_jetbrains_skia_shaper_IcuBidiRunIterator__1nMake")
// Needs ForceNativeThreadState: copies the whole text to UTF-16 and runs bidi analysis over all of it, so its time
// grows with the text length.
// Native call path: MakeIcuBiDiRunIterator -> UTF8ToUTF16 -> makeBidiIterator -> ubidi_setPara
@ForceNativeThreadState
private external fun _nMake(textPtr: NativePointer, bidiLevel: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_IcuBidiRunIterator__1nGetCurrentLevel")
// No ForceNativeThreadState: only returns a stored level, so it is short and cannot block.
// Native call path: SkUnicodeBidiRunIterator::currentLevel
private external fun _nGetCurrentLevel(ptr: NativePointer): Int
