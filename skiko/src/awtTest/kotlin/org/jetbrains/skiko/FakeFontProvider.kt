package org.jetbrains.skiko

import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface
import org.jetbrains.skiko.FontFamily.FontFamilySource

internal class FakeFontProvider(
    private val families: MutableMap<FontFamilyKey, FontFamily> = mutableMapOf()
) : FontProvider {

    var lastGetName: String? = null
        private set
    var lastContainsName: String? = null
        private set

    constructor(vararg families: FontFamily) : this(
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

    override suspend fun getFontFamilyOrNull(familyName: String): FontFamily? {
        val key = FontFamilyKey(familyName)
        lastGetName = familyName
        return families[key]
    }

    fun addEmptyFontFamily(familyName: String, source: FontFamilySource = FontFamilySource.Custom): FontFamily {
        val key = FontFamilyKey(familyName)
        val family = FontFamily(familyName, source)
        families += key to family
        return family
    }

    fun resetNamesTracking() {
        lastGetName = null
    }
}
