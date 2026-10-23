package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.paragraph.TypefaceFontProvider

open class FontMgr : RefCnt {
    companion object {
        init {
            staticLoad()
        }

        val default = FontMgr(_nDefault(), false)
    }

    val familiesCount: Int
        get() = try {
            Stats.onNativeCall()
            _nGetFamiliesCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun getFamilyName(index: Int): String =
        try {
            Stats.onNativeCall()
            withStringResult {
                _nGetFamilyName(_ptr, index)
            }
        } finally {
            reachabilityBarrier(this)
        }

    fun makeStyleSet(index: Int): FontStyleSet? =
        try {
            Stats.onNativeCall()
            val ptr = _nMakeStyleSet(_ptr, index)
            if (ptr == NullPointer) null else FontStyleSet(ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * The caller must call [close] on the returned object.
     * Never returns `null`; will return an empty set if the name is not found.
     *
     * Passing `null` as the parameter will return the default system family.
     * Note that most systems don't have a default system family, so passing
     * `null` will often result in the empty set.
     *
     * It is possible that this will return a style set not accessible from
     * [makeStyleSet] due to hidden or auto-activated fonts.
     */
    fun matchFamily(familyName: String?): FontStyleSet =
        try {
            Stats.onNativeCall()
            interopScope { FontStyleSet(_nMatchFamily(_ptr, toInterop(familyName))) }
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Find the closest matching typeface to the specified [familyName] and [style]
     * and return it. The caller must call [close] on the returned
     * object. Will return `null` if no 'good' match is found.
     *
     * Passing `null` as the value for [familyName] will return the
     * default system font on certain targets but it's not guaranteed
     * to be available.
     *
     * It is possible that this will return a style set not accessible from
     * [makeStyleSet] or [matchFamily] due to hidden or
     * auto-activated fonts.
     */
    fun matchFamilyStyle(familyName: String?, style: FontStyle): Typeface? =
        try {
            Stats.onNativeCall()
            val ptr = interopScope { _nMatchFamilyStyle(_ptr, toInterop(familyName), style._value) }
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun matchFamiliesStyle(families: Array<String?>, style: FontStyle): Typeface? {
        for (family in families) {
            val typeface = matchFamilyStyle(family, style)
            if (typeface != null) return typeface
        }
        return null
    }

    /**
     * Use the system fallback to find a typeface for the given character.
     * Note that bcp47 is a combination of ISO 639, 15924, and 3166-1 codes,
     * so it is fine to just pass a ISO 639 here.
     *
     * Will return `null` if no family can be found for the character
     * in the system fallback.
     *
     * Passing `null` as the value for [familyName] will return the
     * default system font.
     *
     * `bcp47[0]` is the least significant fallback, `bcp47[bcp47.length-1]` is the
     * most significant. If no specified bcp47 codes match, any font with the
     * requested character will be matched.
     */
    fun matchFamilyStyleCharacter(
        familyName: String?,
        style: FontStyle,
        bcp47: Array<String>?,
        character: Int
    ): Typeface? =
        try {
            Stats.onNativeCall()
            val ptr = interopScope {
                _nMatchFamilyStyleCharacter(
                    _ptr,
                    toInterop(familyName),
                    style._value,
                    toInterop(bcp47 ?: emptyArray()),
                    bcp47?.size ?: 0,
                    character
                )
            }
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun matchFamiliesStyleCharacter(
        families: Array<String?>,
        style: FontStyle,
        bcp47: Array<String>?,
        character: Int
    ): Typeface? {
        for (family in families) {
            val typeface = matchFamilyStyleCharacter(family, style, bcp47, character)
            if (typeface != null) return typeface
        }
        return null
    }

    /**
     * Create a typeface for the specified data and TTC index (pass 0 for none)
     * or null if the data is not recognized. The caller must call [close] on
     * the returned object if it is not null.
     */
    fun makeFromData(data: Data?, ttcIndex: Int = 0): Typeface? =
        try {
            Stats.onNativeCall()
            val ptr =
                _nMakeFromData(_ptr, getPtr(data), ttcIndex)
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(data)
        }

    fun makeFromFile(path: String, ttcIndex: Int = 0): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = interopScope {
                _nMakeFromFile(_ptr, toInterop(path), ttcIndex)
            }
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun legacyMakeTypeface(name: String, style: FontStyle): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = interopScope {
                _nLegacyMakeTypeface(_ptr, toInterop(name), style._value)
            }
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    internal constructor(ptr: NativePointer) : super(ptr)

    internal constructor(ptr: NativePointer, allowClose: Boolean) : super(ptr, allowClose)
}


@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nGetFamiliesCount")
// Needs ForceNativeThreadState: calls into the DirectWrite font library, whose code cannot be shown to avoid blocking
// or long work.
// Native call path: SkFontMgr_DirectWrite::onCountFamilies -> GetFontFamilyCount
@ForceNativeThreadState
private external fun _nGetFamiliesCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nGetFamilyName")
// Needs ForceNativeThreadState: reads a font family name through CoreFoundation, an Apple platform API that converts it
// to a string.
// Native call path: SkFontMgr_Mac::onGetFamilyName -> SkStringFromCFString
@ForceNativeThreadState
private external fun _nGetFamilyName(ptr: NativePointer, index: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nMakeStyleSet")
// Needs ForceNativeThreadState: asks CoreText for the font descriptors of a family and builds a style set from them.
// Native call path: SkFontMgr_Mac::onCreateStyleSet -> CreateSet -> CTFontDescriptorCreateMatchingFontDescriptors
@ForceNativeThreadState
private external fun _nMakeStyleSet(ptr: NativePointer, index: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nMatchFamily")
// Needs ForceNativeThreadState: asks CoreText to match a font family name and builds a style set from the result.
// Native call path: SkFontMgr_Mac::onMatchFamily -> CreateSet -> CTFontDescriptorCreateMatchingFontDescriptors
@ForceNativeThreadState
private external fun _nMatchFamily(ptr: NativePointer, familyName: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nMatchFamilyStyle")
// Needs ForceNativeThreadState: asks CoreText for the one font that matches a family and style, and creates a typeface
// from it.
// Native call path: SkFontMgr_Mac::onMatchFamilyStyle -> CTFontDescriptorCreateMatchingFontDescriptor ->
//   create_from_desc
@ForceNativeThreadState
private external fun _nMatchFamilyStyle(ptr: NativePointer, familyName: InteropPointer, fontStyle: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nMatchFamilyStyleCharacter")
// Needs ForceNativeThreadState: asks CoreText for a font that can draw a given character, and creates a typeface from
// it.
// Native call path: SkFontMgr_Mac::onMatchFamilyStyleCharacter -> CTFontCreateForStringWithLanguage ->
//   SkTypeface_Mac::Make
@ForceNativeThreadState
private external fun _nMatchFamilyStyleCharacter(
    ptr: NativePointer,
    familyName: InteropPointer,
    fontStyle: Int,
    bcp47: InteropPointer,
    bcp47size: Int,
    character: Int
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nMakeFromData")
// Needs ForceNativeThreadState: loads a font from a block of bytes in memory through CoreText.
// Native call path: SkFontMgr_Mac::onMakeFromData -> SkTypeface_Mac::MakeFromStream -> ctfont_from_skdata
@ForceNativeThreadState
private external fun _nMakeFromData(ptr: NativePointer, dataPtr: NativePointer, ttcIndex: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nMakeFromFile")
// Needs ForceNativeThreadState: reads a font file from disk, then loads its bytes through CoreText.
// Native call path: SkFontMgr_Mac::onMakeFromFile -> SkData::MakeFromFileName -> onMakeFromData
@ForceNativeThreadState
private external fun _nMakeFromFile(ptr: NativePointer, pathPtr: InteropPointer, ttcIndex: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nDefault")
// Needs ForceNativeThreadState: builds the list of installed fonts by asking CoreText for the full font collection.
// Native call path: SkFontMgrSkikoDefault -> SkFontMgr_New_CoreText -> SkFontMgr_Mac::SkFontMgr_Mac
@ForceNativeThreadState
private external fun _nDefault(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgr__1nLegacyMakeTypeface")
// Needs ForceNativeThreadState: asks CoreText to create a font from a family name and style, and creates a typeface
// from it.
// Native call path: SkFontMgr_Mac::onLegacyMakeTypeface -> create_from_name -> CTFontCreateWithFontDescriptor
@ForceNativeThreadState
private external fun _nLegacyMakeTypeface(ptr: NativePointer, familyName: InteropPointer, fontStyle: Int): NativePointer
