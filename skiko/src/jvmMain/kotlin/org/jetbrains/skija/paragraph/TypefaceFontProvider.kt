package org.jetbrains.skija.paragraph

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.skija.Typeface
import org.jetbrains.skija.FontMgr
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class TypefaceFontProvider : FontMgr(_nMake()) {
    companion object {
        @JvmStatic external fun _nMake(): Long
        @JvmStatic external fun _nRegisterTypeface(ptr: Long, typefacePtr: Long, alias: String?): Long

        init {
            staticLoad()
        }
    }

    @JvmOverloads
    fun registerTypeface(typeface: Typeface?, alias: String? = null): TypefaceFontProvider {
        return try {
            Stats.onNativeCall()
            _nRegisterTypeface(
                _ptr,
                Native.getPtr(typeface),
                alias
            )
            this
        } finally {
            Reference.reachabilityFence(typeface)
        }
    }

    init {
        Stats.onNativeCall()
    }
}