package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
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

    val fontManagersCount: Int
        get() = try {
            Stats.onNativeCall()
            _nGetFontManagersCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun setAssetFontManager(fontMgr: FontMgr?): FontCollection {
        return try {
            Stats.onNativeCall()
            _nSetAssetFontManager(_ptr, getPtr(fontMgr), NullPointer)
            this
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(fontMgr)
        }
    }

    fun setDynamicFontManager(fontMgr: FontMgr?): FontCollection {
        return try {
            Stats.onNativeCall()
            _nSetDynamicFontManager(
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
            _nSetTestFontManager(_ptr, getPtr(fontMgr), NullPointer)
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
                _nSetDefaultFontManager(
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
            val ptr = _nGetFallbackManager(_ptr)
            if (ptr == NullPointer) null else FontMgr(ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun findTypefaces(familyNames: Array<String>?, style: FontStyle): Array<Typeface?> {
        return try {
            Stats.onNativeCall()
            arrayDecoderScope({  ArrayDecoder(interopScope {
                _nFindTypefaces(_ptr, toInterop(familyNames), familyNames?.size ?: 0, style._value)
            }, RefCnt_nGetFinalizer()) }) { arrayDecoder ->
                (0 until arrayDecoder.size).map { i ->
                    Typeface(arrayDecoder.release(i))
                }.toTypedArray()
            }
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(familyNames)
        }
    }

    fun defaultFallback(unicode: Int, familyNames: Array<String>?, style: FontStyle, locale: String?): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = interopScope {
                _nDefaultFallbackChar(
                    _ptr,
                    unicode,
                    toInterop(familyNames),
                    familyNames?.size ?: 0,
                    style._value,
                    toInterop(locale)
                )
            }
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(familyNames)
            reachabilityBarrier(locale)
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

// Runnable: constructs FontCollection and ParagraphCache members; no typeface, font manager or Apple object
@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nMake")
private external fun _nMake(): NativePointer

// Runnable: getFontManagerOrder copies at most four sk_sp members; the unrefs are not the last
@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nGetFontManagersCount")
private external fun _nGetFontManagersCount(ptr: NativePointer): Int

// Native: FontCollection::setAssetFontManager drops the old SkFontMgr -> ~SkFontMgr_Mac -> CFRelease
@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetAssetFontManager")
@ForceNativeThreadState
private external fun _nSetAssetFontManager(ptr: NativePointer, fontManagerPtr: NativePointer, defaultFamilyNameStr: NativePointer)

// Native: FontCollection::setDynamicFontManager drops the old SkFontMgr -> ~SkFontMgr_Mac -> CFRelease
@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetDynamicFontManager")
@ForceNativeThreadState
private external fun _nSetDynamicFontManager(ptr: NativePointer, fontManagerPtr: NativePointer, defaultFamilyNameStr: NativePointer)

// Native: FontCollection::setTestFontManager drops the old SkFontMgr -> ~SkFontMgr_Mac -> CFRelease
@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetTestFontManager")
@ForceNativeThreadState
private external fun _nSetTestFontManager(ptr: NativePointer, fontManagerPtr: NativePointer, defaultFamilyNameStr: NativePointer)

// Native: FontCollection::setDefaultFontManager drops the old SkFontMgr -> ~SkFontMgr_Mac -> CFRelease
@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetDefaultFontManager")
@ForceNativeThreadState
private external fun _nSetDefaultFontManager(ptr: NativePointer, fontManagerPtr: NativePointer, defaultFamilyName: InteropPointer)

// Runnable: returns an sk_sp copy of fDefaultFontManager, released to the caller; ref++ only
@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nGetFallbackManager")
private external fun _nGetFallbackManager(ptr: NativePointer): NativePointer

// Native: FontCollection::findTypefaces -> matchTypeface -> SkFontMgr_Mac::onMatchFamily -> CTFontDescriptor APIs
@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nFindTypefaces")
@ForceNativeThreadState
private external fun _nFindTypefaces(ptr: NativePointer, familyNames: InteropPointer, len: Int, fontStyle: Int): NativePointer

// Native: FontCollection::defaultFallback -> SkFontMgr_Mac::onMatchFamilyStyleCharacter -> CTFontCreateForString
@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallbackChar")
@ForceNativeThreadState
private external fun _nDefaultFallbackChar(
    ptr: NativePointer,
    unicode: Int,
    familyNames: InteropPointer,
    len: Int,
    fontStyle: Int,
    locale: InteropPointer
): NativePointer

// Native: FontCollection::defaultFallback -> SkFontMgr::matchFamilyStyle -> SkFontMgr_Mac::onMatchFamilyStyle
@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallback")
@ForceNativeThreadState
private external fun _nDefaultFallback(ptr: NativePointer): NativePointer

// Runnable: enableFontFallback and disableFontFallback only write the fEnableFontFallback flag
@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetEnableFallback")
private external fun _nSetEnableFallback(ptr: NativePointer, value: Boolean)

// Runnable: returns the address of the fParagraphCache member
@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nGetParagraphCache")
private external fun _nGetParagraphCache(ptr: NativePointer): NativePointer
