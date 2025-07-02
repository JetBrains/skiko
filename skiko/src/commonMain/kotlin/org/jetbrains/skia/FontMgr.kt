package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.withStringResult

open class FontMgr : RefCnt {
    companion object {
        init {
            staticLoad()
        }

        val default = FontMgr(FontMgr_nDefault(), false)
    }

    val familiesCount: Int
        get() = try {
            Stats.onNativeCall()
            FontMgr_nGetFamiliesCount(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    fun getFamilyName(index: Int): String =
        try {
            Stats.onNativeCall()
            withStringResult {
                FontMgr_nGetFamilyName(_ptr, index)
            }
        } finally {
            reachabilityBarrier(this)
        }

    fun makeStyleSet(index: Int): FontStyleSet? =
        try {
            Stats.onNativeCall()
            val ptr = FontMgr_nMakeStyleSet(_ptr, index)
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
            interopScope { FontStyleSet(FontMgr_nMatchFamily(_ptr, toInterop(familyName))) }
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * Find the closest matching typeface to the specified [familyName] and [style]
     * and return it. The caller must call [close] on the returned
     * object. Will return `null` if no 'good' match is found.
     *
     * Passing `null` as the value for [familyName] will return the
     * default system font.
     *
     * It is possible that this will return a style set not accessible from
     * [makeStyleSet] or [matchFamily] due to hidden or
     * auto-activated fonts.
     */
    fun matchFamilyStyle(familyName: String?, style: FontStyle): Typeface? =
        try {
            Stats.onNativeCall()
            val ptr = interopScope { FontMgr_nMatchFamilyStyle(_ptr, toInterop(familyName), style._value) }
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
                FontMgr_nMatchFamilyStyleCharacter(
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
                FontMgr_nMakeFromData(_ptr, getPtr(data), ttcIndex)
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(data)
        }

    fun makeFromFile(path: String, ttcIndex: Int = 0): Typeface? {
        return try {
            Stats.onNativeCall()
            val ptr = interopScope {
                FontMgr_nMakeFromFile(_ptr, toInterop(path), ttcIndex)
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
                FontMgr_nLegacyMakeTypeface(_ptr, toInterop(name), style._value)
            }
            if (ptr == NullPointer) null else Typeface(ptr)
        } finally {
            reachabilityBarrier(this)
        }
    }

    internal constructor(ptr: NativePointer) : super(ptr)

    internal constructor(ptr: NativePointer, allowClose: Boolean) : super(ptr, allowClose)
}