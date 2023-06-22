package org.jetbrains.skiko.awt.font

import org.jetbrains.skiko.InternalSkikoApi

/**
 * Used to transparently provide non-case-sensitive font family name matching,
 * as Skia will match font family names regardless of casing.
 */
@InternalSkikoApi
class FontFamilyKey(val familyName: String) : Comparable<String> {

    val identifier = familyName.lowercase()

    override fun compareTo(other: String) =
        identifier.compareTo(other.lowercase())

    @Suppress("RedundantIf") // Auto-generated
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FontFamilyKey

        if (identifier != other.identifier) return false

        return true
    }

    override fun hashCode() = identifier.hashCode()

    override fun toString(): String = "FontFamilyKey(familyName='$familyName')"


    object Apple {
        val SystemFont = FontFamilyKey("System Font")
        val AppleSystemUiFont = FontFamilyKey(".AppleSystemUIFont")

        val hiddenSystemFontNames = setOf(SystemFont, AppleSystemUiFont)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    object Awt {
        val Serif = FontFamilyKey("Serif")
        val SansSerif = FontFamilyKey("SansSerif")
        val Monospaced = FontFamilyKey("Monospaced")
        val Dialog = FontFamilyKey("Dialog")
        val DialogInput = FontFamilyKey("DialogInput")

        val awtLogicalFonts = setOf(Serif, SansSerif, Monospaced, Dialog, DialogInput)
    }
}
