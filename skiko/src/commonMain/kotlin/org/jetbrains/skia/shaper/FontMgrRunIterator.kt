@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr

class FontMgrRunIterator(text: ManagedString?, manageText: Boolean, font: Font?, opts: ShapingOptions) :
    ManagedRunIterator<FontRun?>(
        _nMake(getPtr(text), getPtr(font), opts), text, manageText
    ) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor(text: String?, font: Font?, opts: ShapingOptions) : this(ManagedString(text), true, font, opts) {}
    constructor(text: String?, font: Font?) : this(ManagedString(text), true, font, ShapingOptions.DEFAULT) {}

    override operator fun next(): FontRun {
        return try {
            _nConsume(_ptr)
            FontRun(_getEndOfCurrentRun(), Font(_nGetCurrentFont(_ptr)))
        } finally {
            reachabilityBarrier(this)
        }
    }

    init {
        Stats.onNativeCall()
        reachabilityBarrier(text)
        reachabilityBarrier(font)
        reachabilityBarrier(opts)
    }

    override fun remove() {
        TODO("Not yet implemented")
    }
}


@ExternalSymbolName("org_jetbrains_skia_FontMgrRunIterator__1nMake")
private external fun _nMake(textPtr: NativePointer, fontPtr: NativePointer, opts: ShapingOptions?): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgrRunIterator__1nGetCurrentFont")
private external fun _nGetCurrentFont(ptr: NativePointer): NativePointer
