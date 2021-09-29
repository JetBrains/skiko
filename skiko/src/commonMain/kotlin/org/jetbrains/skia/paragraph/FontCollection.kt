package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.*

class FontCollection internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        init {
            staticLoad()
        }
    }

    constructor() : this(_nMake()) {
        Stats.onNativeCall()
    }

    val fontManagersCount: NativePointer
        get() = try {
            Stats.onNativeCall()
            _nGetFontManagersCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun setAssetFontManager(fontMgr: FontMgr?): FontCollection {
        return try {
            Stats.onNativeCall()
            _nSetAssetFontManager(_ptr, getPtr(fontMgr))
            this
        } finally {
            reachabilityBarrier(fontMgr)
        }
    }

    fun setDynamicFontManager(fontMgr: FontMgr?): FontCollection {
        return try {
            Stats.onNativeCall()
            _nSetDynamicFontManager(
                _ptr,
                getPtr(fontMgr)
            )
            this
        } finally {
            reachabilityBarrier(fontMgr)
        }
    }

    fun setTestFontManager(fontMgr: FontMgr?): FontCollection {
        return try {
            Stats.onNativeCall()
            _nSetTestFontManager(_ptr, getPtr(fontMgr))
            this
        } finally {
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
                _nSetDefaultFontManager(
                    _ptr,
                    getPtr(fontMgr),
                    toInterop(defaultFamilyName)
                )
            }
            this
        } finally {
            reachabilityBarrier(fontMgr)
        }
    }

    val fallbackManager: FontMgr?
        get() = try {
            Stats.onNativeCall()
            val ptr = _nGetFallbackManager(_ptr)
            if (ptr == NullPointer) null else FontMgr(ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun findTypefaces(familyNames: Array<String>?, style: FontStyle): Array<Typeface?> {
        return try {
            Stats.onNativeCall()
            val ptrs = interopScope {
                _nFindTypefaces(_ptr, toInterop(familyNames), style._value).fromInteropNativePointerArray()
            }
            val res = arrayOfNulls<Typeface>(ptrs.size)
            for (i in 0 until ptrs.size) res[i] = Typeface(ptrs[i])
            res
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun defaultFallback(unicode: Int, style: FontStyle, locale: String?): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = _nDefaultFallbackChar(_ptr, unicode, style._value, locale)
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun defaultFallback(): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = _nDefaultFallback(_ptr)
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun setEnableFallback(value: Boolean): FontCollection {
        Stats.onNativeCall()
        _nSetEnableFallback(_ptr, value)
        return this
    }

    val paragraphCache: ParagraphCache
        get() = try {
            Stats.onNativeCall()
            ParagraphCache(this, _nGetParagraphCache(_ptr))
        } finally {
            reachabilityBarrier(this)
        }
}


@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nMake")
private external fun _nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nGetFontManagersCount")
private external fun _nGetFontManagersCount(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetAssetFontManager")
private external fun _nSetAssetFontManager(ptr: NativePointer, fontManagerPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetDynamicFontManager")
private external fun _nSetDynamicFontManager(ptr: NativePointer, fontManagerPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetTestFontManager")
private external fun _nSetTestFontManager(ptr: NativePointer, fontManagerPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetDefaultFontManager")
private external fun _nSetDefaultFontManager(ptr: NativePointer, fontManagerPtr: NativePointer, defaultFamilyName: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nGetFallbackManager")
private external fun _nGetFallbackManager(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nFindTypefaces")
private external fun _nFindTypefaces(ptr: NativePointer, familyNames: InteropPointer, fontStyle: Int): InteropPointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallbackChar")
private external fun _nDefaultFallbackChar(ptr: NativePointer, unicode: Int, fontStyle: Int, locale: String?): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallback")
private external fun _nDefaultFallback(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetEnableFallback")
private external fun _nSetEnableFallback(ptr: NativePointer, value: Boolean): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nGetParagraphCache")
private external fun _nGetParagraphCache(ptr: NativePointer): NativePointer
