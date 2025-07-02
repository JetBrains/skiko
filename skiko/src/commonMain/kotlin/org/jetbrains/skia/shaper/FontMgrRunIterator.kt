@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia.shaper

import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.ManagedString
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier

private fun makeHbIcuScriptRunIterator(
    text: ManagedString,
    font: Font,
    opts: ShapingOptions
): NativePointer {
    Stats.onNativeCall()
    return try {
        FontMgrRunIterator_nMake(getPtr(text), getPtr(font), getPtr(opts.fontMgr), opts._booleanPropsToInt())
    } finally {
        reachabilityBarrier(text)
        reachabilityBarrier(font)
        reachabilityBarrier(opts.fontMgr)
    }
}

class FontMgrRunIterator(text: ManagedString, manageText: Boolean, font: Font, opts: ShapingOptions) :
    ManagedRunIterator<FontRun?>(
        makeHbIcuScriptRunIterator(text, font, opts), text, manageText
    ) {
    companion object {
        init {
            staticLoad()
        }
    }

    private val _font: Font = font
    private val _fontMgr: FontMgr? = opts.fontMgr

    constructor(text: String, font: Font, opts: ShapingOptions) : this(ManagedString(text), true, font, opts)
    constructor(text: String, font: Font) : this(ManagedString(text), true, font, ShapingOptions.DEFAULT)

    override operator fun next(): FontRun {
        return try {
            ManagedRunIterator_nConsume(_ptr)
            FontRun(_getEndOfCurrentRun(), Font(FontMgrRunIterator_nGetCurrentFont(_ptr)))
        } finally {
            reachabilityBarrier(this)
        }
    }

    override fun remove() {
        TODO("Not yet implemented")
    }
}