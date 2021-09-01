package org.jetbrains.skija.paragraph

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.skija.*
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference

class FontCollection internal constructor(ptr: Long) : RefCnt(ptr) {
    companion object {
        @JvmStatic external fun _nMake(): Long
        @JvmStatic external fun _nGetFontManagersCount(ptr: Long): Long
        @JvmStatic external fun _nSetAssetFontManager(ptr: Long, fontManagerPtr: Long): Long
        @JvmStatic external fun _nSetDynamicFontManager(ptr: Long, fontManagerPtr: Long): Long
        @JvmStatic external fun _nSetTestFontManager(ptr: Long, fontManagerPtr: Long): Long
        @JvmStatic external fun _nSetDefaultFontManager(ptr: Long, fontManagerPtr: Long, defaultFamilyName: String?): Long
        @JvmStatic external fun _nGetFallbackManager(ptr: Long): Long
        @JvmStatic external fun _nFindTypefaces(ptr: Long, familyNames: Array<String?>?, fontStyle: Int): LongArray
        @JvmStatic external fun _nDefaultFallbackChar(ptr: Long, unicode: Int, fontStyle: Int, locale: String?): Long
        @JvmStatic external fun _nDefaultFallback(ptr: Long): Long
        @JvmStatic external fun _nSetEnableFallback(ptr: Long, value: Boolean): Long
        @JvmStatic external fun _nGetParagraphCache(ptr: Long): Long

        init {
            staticLoad()
        }
    }

    constructor() : this(_nMake()) {
        Stats.onNativeCall()
    }

    val fontManagersCount: Long
        get() = try {
            Stats.onNativeCall()
            _nGetFontManagersCount(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun setAssetFontManager(fontMgr: FontMgr?): FontCollection {
        return try {
            Stats.onNativeCall()
            _nSetAssetFontManager(_ptr, Native.Companion.getPtr(fontMgr))
            this
        } finally {
            Reference.reachabilityFence(fontMgr)
        }
    }

    fun setDynamicFontManager(fontMgr: FontMgr?): FontCollection {
        return try {
            Stats.onNativeCall()
            _nSetDynamicFontManager(
                _ptr,
                Native.Companion.getPtr(fontMgr)
            )
            this
        } finally {
            Reference.reachabilityFence(fontMgr)
        }
    }

    fun setTestFontManager(fontMgr: FontMgr?): FontCollection {
        return try {
            Stats.onNativeCall()
            _nSetTestFontManager(_ptr, Native.Companion.getPtr(fontMgr))
            this
        } finally {
            Reference.reachabilityFence(fontMgr)
        }
    }

    fun setDefaultFontManager(fontMgr: FontMgr?): FontCollection {
        return setDefaultFontManager(fontMgr, null)
    }

    fun setDefaultFontManager(fontMgr: FontMgr?, defaultFamilyName: String?): FontCollection {
        return try {
            Stats.onNativeCall()
            _nSetDefaultFontManager(
                _ptr,
                Native.Companion.getPtr(fontMgr),
                defaultFamilyName
            )
            this
        } finally {
            Reference.reachabilityFence(fontMgr)
        }
    }

    val fallbackManager: FontMgr?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetFallbackManager(_ptr)
            if (ptr == 0L) null else FontMgr(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    fun findTypefaces(familyNames: Array<String?>?, style: FontStyle): Array<Typeface?> {
        return try {
            Stats.onNativeCall()
            val ptrs = _nFindTypefaces(_ptr, familyNames, style._value)
            val res = arrayOfNulls<Typeface>(ptrs.size)
            for (i in ptrs.indices) res[i] = Typeface(ptrs[i])
            res
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun defaultFallback(unicode: Int, style: FontStyle, locale: String?): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = _nDefaultFallbackChar(_ptr, unicode, style._value, locale)
            if (ptr == 0L) null else Typeface(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun defaultFallback(): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = _nDefaultFallback(_ptr)
            if (ptr == 0L) null else Typeface(ptr)
        } finally {
            Reference.reachabilityFence(this)
        }
    }

    fun setEnableFallback(value: Boolean): FontCollection {
        Stats.onNativeCall()
        _nSetEnableFallback(_ptr, value)
        return this
    }

    val paragraphCache: org.jetbrains.skija.paragraph.ParagraphCache
        get() = try {
            Stats.onNativeCall()
            ParagraphCache(this, _nGetParagraphCache(_ptr))
        } finally {
            Reference.reachabilityFence(this)
        }
}