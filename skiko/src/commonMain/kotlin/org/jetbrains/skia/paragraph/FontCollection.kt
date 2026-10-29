package org.jetbrains.skia.paragraph

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.*
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ForceNativeThreadState
import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Native.Companion.NullPointer

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
        get() = nativeCall { _nGetFontManagersCount(_ptr) }

    fun setAssetFontManager(fontMgr: FontMgr?): FontCollection = nativeCall(fontMgr) {fontMgr ->
        _nSetAssetFontManager(_ptr, fontMgr.pointerOrNullPointer, NullPointer)
        this
    }

    fun setDynamicFontManager(fontMgr: FontMgr?): FontCollection = nativeCall(fontMgr) {fontMgr ->
        _nSetDynamicFontManager(_ptr, fontMgr.pointerOrNullPointer, NullPointer)
        this
    }

    fun setTestFontManager(fontMgr: FontMgr?): FontCollection = nativeCall(fontMgr) {fontMgr ->
        _nSetTestFontManager(_ptr, fontMgr.pointerOrNullPointer, NullPointer)
        this
    }

    fun setDefaultFontManager(fontMgr: FontMgr?): FontCollection {
        return setDefaultFontManager(fontMgr, null)
    }

    fun setDefaultFontManager(fontMgr: FontMgr?, defaultFamilyName: String?): FontCollection = nativeCall(fontMgr) {fontMgr ->
        interopScope {
            _nSetDefaultFontManager(
                _ptr,
                fontMgr.pointerOrNullPointer,
                toInterop(defaultFamilyName)
            )
        }
        this
    }

    val fallbackManager: FontMgr?
        get() = nativeCall {
            val ptr = _nGetFallbackManager(_ptr)
            if (ptr == NullPointer) null else FontMgr(ptr)
        }

    fun findTypefaces(familyNames: Array<String>?, style: FontStyle): Array<Typeface?> = nativeCall {
        arrayDecoderScope({  ArrayDecoder(interopScope {
            _nFindTypefaces(_ptr, toInterop(familyNames), familyNames?.size ?: 0, style._value)
        }, RefCnt_nGetFinalizer()) }) { arrayDecoder ->
            (0 until arrayDecoder.size).map { i ->
                Typeface(arrayDecoder.release(i))
            }.toTypedArray()
        }
    }

    fun defaultFallback(unicode: Int, familyNames: Array<String>?, style: FontStyle, locale: String?): Typeface? = nativeCall {
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
    }

    fun defaultFallback(): Typeface? = nativeCall {
        val ptr = _nDefaultFallback(_ptr)
        if (ptr == NullPointer) null else Typeface(ptr)
    }

    fun setEnableFallback(value: Boolean): FontCollection = nativeCall {
        _nSetEnableFallback(_ptr, value)
        this
    }

    val paragraphCache: ParagraphCache
        get() = nativeCall { ParagraphCache(this, _nGetParagraphCache(_ptr)) }
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
