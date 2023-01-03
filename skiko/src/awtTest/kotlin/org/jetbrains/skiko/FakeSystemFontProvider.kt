package org.jetbrains.skiko

import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface

internal class FakeSystemFontProvider(
    val families: MutableMap<FontFamilyKey, SkikoFontFamily> = mutableMapOf()
) : SystemFontProvider {

    var lastGetName: String? = null
        private set
    var lastContainsName: String? = null
        private set

    constructor(vararg families: SkikoFontFamily) : this(
        families.associateBy { FontFamilyKey(it.familyName) }
            .toMutableMap()
    )

    override fun invalidate() {
        // Do nothing
    }

    override suspend fun familyNames() =
        families.keys
            .map { it.familyName }
            .toSet()

    override suspend fun contains(familyName: String): Boolean {
        lastContainsName = familyName
        return familyNames().contains(familyName)
    }

    override suspend fun getTypefaceOrNull(familyName: String, fontStyle: FontStyle): Typeface? {
        val key = FontFamilyKey(familyName)
        lastGetName = familyName
        return families[key]?.get(fontStyle)
    }

    override suspend fun getFontFamilyOrNull(familyName: String): SkikoFontFamily? {
        val key = FontFamilyKey(familyName)
        lastGetName = familyName
        return families[key]
    }

    fun addEmptyFontFamily(familyName: String): SkikoFontFamily {
        val key = FontFamilyKey(familyName)
        val family = SkikoFontFamily(familyName)
        families += key to family
        return family
    }

    fun resetNamesTracking() {
        lastGetName = null
    }
}
