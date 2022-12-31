package org.jetbrains.skiko

import org.jetbrains.skia.FontFamilyName
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.FontStyleSet
import org.jetbrains.skia.Typeface

// TODO does this need to be closeable? Are we leaking memory?
class FontFamilyInfo internal constructor(
    private val typefacesByStyle: Map<FontStyle, Typeface>
) : Map<FontStyle, Typeface> by typefacesByStyle {

    internal constructor(styleSet: FontStyleSet) : this(
        convertToMap(styleSet)
    )

    val availableStyles
        get() = typefacesByStyle.keys

    val availableTypefaces
        get() = typefacesByStyle.values.toSet()

    operator fun plus(typeface: Typeface) = addTypeface(typeface)

    /**
     * Adds the specified [typeface] to this [FontFamilyInfo].
     *
     * If the font family already contains a [Typeface] with the same [FontStyle],
     * this **will replace** the previous value.
     */
    fun addTypeface(typeface: Typeface): FontFamilyInfo {
        if (typefacesByStyle.isEmpty()) {
            return fromTypefaces(typeface)
        }

        require(typefacesByStyle.values.firstOrNull()?.isCompatibleWith(typeface) == true) {
            "The provided typeface $typeface is not compatible with this font family, '${typefacesByStyle.values.first().familyName}'"
        }
        return FontFamilyInfo(typefacesByStyle + (typeface.fontStyle to typeface))
    }

    operator fun minus(typeface: Typeface) = removeTypeface(typeface)
    operator fun minus(style: FontStyle) = removeTypefaceByStyle(style)

    /**
     * Removes a typeface from the family if it exists.
     *
     * @throws IllegalStateException if there is a typeface with the same style as [typeface],
     * but it's a different typeface. It's not clear what the intended outcome would be in this case.
     *
     * @see isCompatibleWith
     */
    fun removeTypeface(typeface: Typeface): FontFamilyInfo {
        if (typefacesByStyle.isEmpty()) return this

        val style = typeface.fontStyle
        val existingTypeface = typefacesByStyle[style] ?: return this
        require(typeface.isCompatibleWith(existingTypeface)) {
            "The family contains a typeface with the same style, but it's not the same typeface provided.\n" +
                    "Existing: ${existingTypeface ?: "N/A"}\n" +
                    "Provided: $typeface"
        }
        return removeTypefaceByStyle(style)
    }

    /**
     * Removes the typeface from the family with the given style if it exists.
     */
    fun removeTypefaceByStyle(style: FontStyle) =
        FontFamilyInfo(typefacesByStyle - style)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FontFamilyInfo

        if (typefacesByStyle != other.typefacesByStyle) return false

        return true
    }

    override fun hashCode() = typefacesByStyle.hashCode()

    companion object {

        private fun convertToMap(fontStyleSet: FontStyleSet): Map<FontStyle, Typeface> {
            val typefaceByStyleMap = mutableMapOf<FontStyle, Typeface>()
            val count = fontStyleSet.count()

            for (i in (0 until count)) {
                val typeface = fontStyleSet.getTypeface(i) ?: continue
                typefaceByStyleMap[fontStyleSet.getStyle(i)] = typeface
            }

            return typefaceByStyleMap.toMap()
        }

        /**
         * Creates a new [FontFamilyInfo] instance, comprised of the provided [typefaces].
         *
         * All provided [Typeface]s **must** have the same [Typeface.familyName] and
         * [Typeface.familyNames], or this function will throw an exception.
         * No two elements in [typefaces] are
         * allowed to represent the same [FontStyle], because a family can only contain
         * one typeface for each style.
         */
        fun fromTypefaces(vararg typefaces: Typeface): FontFamilyInfo {
            if (typefaces.isEmpty()) return FontFamilyInfo(emptyMap())

            val expectedTypeface = typefaces.first()

            val map = HashMap<FontStyle, Typeface>(typefaces.size)
            for (typeface in typefaces) {
                require(typeface.isCompatibleWith(expectedTypeface)) {
                    "Not all provided typefaces are compatible with each other " +
                            "(expected: '${expectedTypeface.familyName}', found: $typeface)"
                }

                require(typeface.fontStyle !in map.keys) {
                    "Trying to add a typeface for style ${typeface.fontStyle}, but it already exists."
                }

                map[typeface.fontStyle] = typeface
            }
            return FontFamilyInfo(map)
        }

        /**
         * Checks if a typeface is compatible with another typeface.
         *
         * Two typefaces are compatible when they are identical (same uniqueId, or
         * are deemed equals by Skia), or when they have the same family name(s).
         *
         * Typefaces with the same family name(s) but different styles are compatible;
         * typefaces with different family name(s) are not. Note that this is different
         * from a logical equality check (see [equalsLogically]) since two typefaces
         * can be compatible even when their styles are different, as long as the
         * family name(s) match.
         *
         * @see equalsLogically
         */
        private fun Typeface.isCompatibleWith(other: Typeface) =
            uniqueId == other.uniqueId || nativeEquals(other) ||
                    (familyName == other.familyName && familyNames.equalsLogically(other.familyNames))

        /**
         * Checks if a typeface is logically equivalent to another.
         *
         * Two typefaces are logically equivalent when they have the same family name(s)
         * and style. Note that this is different from a compatibility check (see
         * [isCompatibleWith]) since two typefaces can be compatible even when their styles
         * are different, as long as the family name(s) match.
         *
         * @see isCompatibleWith
         */
        private fun Typeface.equalsLogically(other: Typeface?): Boolean {
            if (other == null) return false

            return familyName == other.familyName &&
                    familyNames.equalsLogically(other.familyNames) &&
                    fontStyle == other.fontStyle
        }

        /**
         * Checks if two arrays of [FontFamilyName]s are logically equivalent.
         *
         * Two arrays of font family names are equivalent when they contain exactly
         * the same elements, regardless of the order.
         */
        private fun Array<FontFamilyName>.equalsLogically(other: Array<FontFamilyName>): Boolean {
            if (size != other.size) return false

            return all { thisName ->
                other.any { it.language == thisName.language && it.name == thisName.name }
            }
        }
    }
}
