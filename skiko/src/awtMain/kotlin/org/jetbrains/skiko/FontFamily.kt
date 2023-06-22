package org.jetbrains.skiko

import org.jetbrains.skia.*

/**
 * Holds info about a font family. Intended to be used as a cache entry,
 * to which typefaces are lazily loaded as new [Typeface]s are loaded.
 *
 * @param familyName The primary font family name. Must not be blank.
 * @param typefacesByStyle A mutable map of typefaces, organised by their style.
 *
 * @see AwtFontManager
 */
class FontFamily(
    val familyName: String,
    val source: FontFamilySource,
    private val typefacesByStyle: MutableMap<FontStyle, Typeface> = mutableMapOf()
) : Map<FontStyle, Typeface> by typefacesByStyle {

    init {
        require(familyName.isNotBlank()) { "The font family name must not be blank" }
    }

    /**
     * The available styles loaded for the font family.
     */
    val availableStyles
        get() = typefacesByStyle.keys

    /**
     * The available typefaces loaded for the font family.
     */
    val availableTypefaces
        get() = typefacesByStyle.values.toSet()

    operator fun plus(typeface: Typeface) =
        FontFamily(familyName, source, typefacesByStyle.toMutableMap())
            .apply { addTypeface(typeface) }

    operator fun plusAssign(typeface: Typeface) = addTypeface(typeface)

    /**
     * Adds the specified [typeface] to this [FontFamily].
     *
     * If the font family already contains a [Typeface] with the same [FontStyle],
     * this **will replace** the previous value.
     *
     * @throws IllegalArgumentException if the [typeface] family name doesn't match
     * this instance's [familyName].
     */
    fun addTypeface(typeface: Typeface) {
        ensureTypefaceIsCompatible(typeface)
        typefacesByStyle += (typeface.fontStyle to typeface)
    }


    operator fun minusAssign(typeface: Typeface) = removeTypeface(typeface)

    operator fun minus(typeface: Typeface) =
        FontFamily(familyName, source, typefacesByStyle.toMutableMap())
            .apply { removeTypeface(typeface) }

    operator fun minusAssign(style: FontStyle) = removeTypefaceByStyle(style)

    operator fun minus(style: FontStyle) =
        FontFamily(familyName, source, typefacesByStyle.toMutableMap())
            .apply { removeTypefaceByStyle(style) }

    /**
     * Removes a typeface from the family if it exists.
     *
     * @throws IllegalArgumentException if the [typeface]'s font family is different from
     * this instance's [familyName].
     */
    fun removeTypeface(typeface: Typeface) {
        if (typefacesByStyle.isEmpty()) return

        val style = typeface.fontStyle
        ensureTypefaceIsCompatible(typeface)

        typefacesByStyle -= style
    }

    private fun ensureTypefaceIsCompatible(typeface: Typeface) {
        val candidateName = typeface.familyName
        require(familyName.equals(candidateName, ignoreCase = true)) {
            "The provided typeface $typeface is not compatible with this font family, '$familyName'"
        }
    }

    /**
     * Removes the typeface from the family with the given style if it exists.
     */
    fun removeTypefaceByStyle(style: FontStyle) {
        typefacesByStyle -= style
    }

    enum class FontFamilySource {
        System,
        JvmEmbedded,
        Custom
    }

    companion object {

        /**
         * Creates a new [FontFamily] instance, comprised of the provided [typefaces].
         *
         * All provided [Typeface]s **must** have the same [Typeface.familyName] and
         * [Typeface.familyNames], or this function will throw an exception.
         * No two elements in [typefaces] are allowed to represent the same [FontStyle],
         * because a family can only contain one typeface for each style.
         */
        fun fromTypefaces(
            familyName: String,
            source: FontFamilySource,
            vararg typefaces: Typeface,
        ): FontFamily {
            if (typefaces.isEmpty()) return FontFamily(familyName, source)

            val map = HashMap<FontStyle, Typeface>(typefaces.size)
            for (typeface in typefaces) {
                require(typeface.familyName.equals(familyName, ignoreCase = true)) {
                    "Not all provided typefaces are compatible with the family name " +
                            "(expected: '$familyName', found: $typeface)"
                }

                require(typeface.fontStyle !in map.keys) {
                    "Trying to add a typeface for style ${typeface.fontStyle}, but it already exists."
                }

                map[typeface.fontStyle] = typeface
            }
            return FontFamily(familyName, source, map)
        }

        /**
         * Find the closest typeface by style
         */
        fun closestStyle(iterable: Iterable<FontStyle>, style: FontStyle): FontStyle? {
            val keys = iterable.toMutableList()

            keys.sortBy { it.weight }
            val closeByWight = keys.find { it.weight >= style.weight } ?: keys.lastOrNull()
            keys.retainAll { it.weight == closeByWight?.weight }

            keys.sortBy { it.width }
            val closeByWidth = keys.find { it.width >= style.width } ?: keys.lastOrNull()
            keys.retainAll { it.width == closeByWidth?.width }

            keys.sortBy { it.slant }
            val closeBySlant = keys.find { it.slant >= style.slant } ?: keys.lastOrNull()
            keys.retainAll { it.slant == closeBySlant?.slant }

            return keys.firstOrNull()
        }
    }
}
