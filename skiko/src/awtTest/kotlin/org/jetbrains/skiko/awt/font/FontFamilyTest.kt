package org.jetbrains.skiko.awt.font

import org.jetbrains.skia.*

private fun Collection<Typeface>.equalsLogically(other: Collection<Typeface>): Boolean {
    if (size != other.size) return false
    return all { thisTypeface ->
        other.any { it.equalsLogically(thisTypeface) }
    }
}

private fun Typeface.equalsLogically(other: Typeface?): Boolean {
    if (other == null) return false

    fun Array<FontFamilyName>.equalsLogically(other: Array<FontFamilyName>): Boolean {
        if (size != other.size) return false
        return all { thisName ->
            other.any { it.language == thisName.language && it.name == thisName.name }
        }
    }

    return familyName == other.familyName &&
            familyNames.equalsLogically(other.familyNames) &&
            fontStyle == other.fontStyle
}
