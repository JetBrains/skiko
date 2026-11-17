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

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nMake")
// No ForceNativeThreadState: builds empty caches and default family names without touching any font manager, so it is
// short and cannot block.
// Native call path: FontCollection::FontCollection
private external fun _nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nGetFontManagersCount")
// No ForceNativeThreadState: copies up to four font manager references and releases them again, so it is short and
// cannot block.
// Native call path: FontCollection::getFontManagersCount -> getFontManagerOrder
private external fun _nGetFontManagersCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetAssetFontManager")
// Needs ForceNativeThreadState: can free the old font manager, which calls CoreFoundation to release its objects.
// Native call path: FontCollection::setAssetFontManager -> ~SkFontMgr_Mac -> CFRelease
@ForceNativeThreadState
private external fun _nSetAssetFontManager(ptr: NativePointer, fontManagerPtr: NativePointer, defaultFamilyNameStr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetDynamicFontManager")
// Needs ForceNativeThreadState: can free the old font manager, which calls CoreFoundation to release its objects.
// Native call path: FontCollection::setDynamicFontManager -> ~SkFontMgr_Mac -> CFRelease
@ForceNativeThreadState
private external fun _nSetDynamicFontManager(ptr: NativePointer, fontManagerPtr: NativePointer, defaultFamilyNameStr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetTestFontManager")
// Needs ForceNativeThreadState: can free the old font manager, which calls CoreFoundation to release its objects.
// Native call path: FontCollection::setTestFontManager -> ~SkFontMgr_Mac -> CFRelease
@ForceNativeThreadState
private external fun _nSetTestFontManager(ptr: NativePointer, fontManagerPtr: NativePointer, defaultFamilyNameStr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetDefaultFontManager")
// Needs ForceNativeThreadState: can free the old font manager, which calls CoreFoundation to release its objects, and
// copies a list of family names.
// Native call path: FontCollection::setDefaultFontManager -> ~SkFontMgr_Mac -> CFRelease
@ForceNativeThreadState
private external fun _nSetDefaultFontManager(ptr: NativePointer, fontManagerPtr: NativePointer, defaultFamilyName: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nGetFallbackManager")
// No ForceNativeThreadState: adds one reference to the default font manager and hands it to the caller, so it is short
// and cannot block.
// Native call path: FontCollection::getFallbackManager
private external fun _nGetFallbackManager(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nFindTypefaces")
// Needs ForceNativeThreadState: on a cache miss, searches CoreText font descriptors for matching fonts, so it can call
// CoreText.
// Native call path: FontCollection::findTypefaces -> matchTypeface -> SkFontMgr_Mac::onMatchFamily
@ForceNativeThreadState
private external fun _nFindTypefaces(ptr: NativePointer, familyNames: InteropPointer, len: Int, fontStyle: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallbackChar")
// Needs ForceNativeThreadState: asks CoreText for a font that can draw a character, with no cache, so it can call
// CoreText.
// Native call path: FontCollection::defaultFallback -> SkFontMgr_Mac::onMatchFamilyStyleCharacter
@ForceNativeThreadState
private external fun _nDefaultFallbackChar(
    ptr: NativePointer,
    unicode: Int,
    familyNames: InteropPointer,
    len: Int,
    fontStyle: Int,
    locale: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallback")
// Needs ForceNativeThreadState: asks CoreText for a fallback font by family and style, looping over the default family
// names, so it can call CoreText.
// Native call path: FontCollection::defaultFallback -> SkFontMgr::matchFamilyStyle -> SkFontMgr_Mac::onMatchFamilyStyle
@ForceNativeThreadState
private external fun _nDefaultFallback(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetEnableFallback")
// No ForceNativeThreadState: sets one on or off flag for font fallback, so it is short and cannot block.
// Native call path: FontCollection::enableFontFallback
private external fun _nSetEnableFallback(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nGetParagraphCache")
// No ForceNativeThreadState: returns the address of a stored cache object without calling anything, so it is short and
// cannot block.
// Native call path: FontCollection::getParagraphCache
private external fun _nGetParagraphCache(ptr: NativePointer): NativePointer
