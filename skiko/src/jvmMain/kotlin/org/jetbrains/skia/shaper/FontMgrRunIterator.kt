package org.jetbrains.skia.shaper

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import java.lang.ref.Reference

class FontMgrRunIterator(text: ManagedString?, manageText: Boolean, font: Font?, opts: ShapingOptions) :
    ManagedRunIterator<FontRun?>(
        _nMake(Native.getPtr(text), Native.getPtr(font), opts), text, manageText
    ) {
    companion object {
        @JvmStatic external fun _nMake(textPtr: Long, fontPtr: Long, opts: ShapingOptions?): Long
        @JvmStatic external fun _nGetCurrentFont(ptr: Long): Long

        init {
            staticLoad()
        }
    }

    constructor(text: String?, font: Font?, opts: ShapingOptions) : this(ManagedString(text), true, font, opts) {}
    constructor(text: String?, font: Font?) : this(ManagedString(text), true, font, ShapingOptions.DEFAULT) {}

    override operator fun next(): FontRun {
        return try {
            _nConsume(_ptr)
            FontRun(_getEndOfCurrentRun(), org.jetbrains.skia.Font(_nGetCurrentFont(_ptr)))
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    init {
        Stats.onNativeCall()
        Reference.reachabilityFence(text)
        Reference.reachabilityFence(font)
        Reference.reachabilityFence(opts)
    }

    override fun remove() {
        TODO("Not yet implemented")
    }
}