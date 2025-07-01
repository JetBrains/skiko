package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ArrayDecoder
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.arrayDecoderScope
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.RefCnt_nGetFinalizer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier

class FontCollection internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor() : this(FontCollection_nMake()) {
        Stats.onNativeCall()
    }

    val fontManagersCount: Int
        get() = try {
            Stats.onNativeCall()
            FontCollection_nGetFontManagersCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun setAssetFontManager(fontMgr: FontMgr?): FontCollection {
        return try {
            Stats.onNativeCall()
            FontCollection_nSetAssetFontManager(_ptr, getPtr(fontMgr), NullPointer)
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(fontMgr)
        }
    }

    fun setDynamicFontManager(fontMgr: FontMgr?): FontCollection {
        return try {
            Stats.onNativeCall()
            FontCollection_nSetDynamicFontManager(
                _ptr,
                getPtr(fontMgr),
                NullPointer
            )
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(fontMgr)
        }
    }

    fun setTestFontManager(fontMgr: FontMgr?): FontCollection {
        return try {
            Stats.onNativeCall()
            FontCollection_nSetTestFontManager(_ptr, getPtr(fontMgr), NullPointer)
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(fontMgr)
        }
    }

    fun setDefaultFontManager(fontMgr: FontMgr?): FontCollection {
        return setDefaultFontManager(fontMgr, null)
    }

    fun setDefaultFontManager(fontMgr: FontMgr?, defaultFamilyName: String?): FontCollection {
        return try {
            Stats.onNativeCall()
            interopScope {
                FontCollection_nSetDefaultFontManager(
                    _ptr,
                    getPtr(fontMgr),
                    toInterop(defaultFamilyName)
                )
            }
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(fontMgr)
        }
    }

    val fallbackManager: FontMgr?
        get() = try {
            Stats.onNativeCall()
            val ptr = FontCollection_nGetFallbackManager(_ptr)
            if (ptr == NullPointer) null else FontMgr(ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun findTypefaces(familyNames: Array<String>?, style: FontStyle): Array<Typeface?> {
        return try {
            Stats.onNativeCall()
            arrayDecoderScope({
                ArrayDecoder(interopScope {
                    FontCollection_nFindTypefaces(_ptr, toInterop(familyNames), familyNames?.size ?: 0, style._value)
                }, RefCnt_nGetFinalizer())
            }) { arrayDecoder ->
                (0 until arrayDecoder.size).map { i ->
                    Typeface(arrayDecoder.release(i))
                }.toTypedArray()
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun defaultFallback(unicode: Int, style: FontStyle, locale: String?): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = interopScope { FontCollection_nDefaultFallbackChar(_ptr, unicode, style._value, toInterop(locale)) }
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun defaultFallback(): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = FontCollection_nDefaultFallback(_ptr)
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun setEnableFallback(value: Boolean): FontCollection {
        Stats.onNativeCall()
        FontCollection_nSetEnableFallback(_ptr, value)
        return this
    }

    val paragraphCache: ParagraphCache
        get() = try {
            Stats.onNativeCall()
            ParagraphCache(this, FontCollection_nGetParagraphCache(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
}