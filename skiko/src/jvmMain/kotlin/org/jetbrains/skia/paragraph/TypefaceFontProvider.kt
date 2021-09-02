package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
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