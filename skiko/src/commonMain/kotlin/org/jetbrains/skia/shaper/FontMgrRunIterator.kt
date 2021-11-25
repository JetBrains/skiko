@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

class FontMgrRunIterator(text: ManagedString?, manageText: Boolean, font: Font?, opts: ShapingOptions) :
    ManagedRunIterator<FontRun?>(
        _nMake(getPtr(text), getPtr(font), getPtr(opts.fontMgr), opts._booleanPropsToInt()), text, manageText
    ) {
    companion object {
        init {
            staticLoad()
        }
    }

    private val _font: Font? = font
    private val _fontMgr: FontMgr? = opts.fontMgr

    constructor(text: String?, font: Font?, opts: ShapingOptions) : this(ManagedString(text), true, font, opts)
    constructor(text: String?, font: Font?) : this(ManagedString(text), true, font, ShapingOptions.DEFAULT)

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

@ExternalSymbolName("org_jetbrains_skia_shaper_FontMgrRunIterator__1nMake")
private external fun _nMake(textPtr: NativePointer, fontPtr: NativePointer, fontMgrPtr: NativePointer, optsBooleanProps: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_shaper_FontMgrRunIterator__1nGetCurrentFont")
private external fun _nGetCurrentFont(ptr: NativePointer): NativePointer
