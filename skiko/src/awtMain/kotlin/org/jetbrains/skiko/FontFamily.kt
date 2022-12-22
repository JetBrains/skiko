package org.jetbrains.skiko

import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.FontStyleSet
import org.jetbrains.skia.Typeface

// TODO does this need to be closeable?
class FontFamily internal constructor(
    private val typefacesByStyle: Map<FontStyle, Typeface>
) : Map<FontStyle, Typeface> by typefacesByStyle {

    internal constructor(styleSet: FontStyleSet) : this(
        convertToMap(styleSet)
    )

    val availableStyles
        get() = typefacesByStyle.keys

    val availableTypefaces
        get() = typefacesByStyle.values.toSet()

    /**
     * Adds the specified [typeface] to this [FontFamily].
     * If the font family already contains a [Typeface] with the same [FontStyle],
     * this **will replace** the previous value.
     */
    fun addTypeface(typeface: Typeface) =
        FontFamily(
            typefacesByStyle.toMutableMap()
                .also { newMap -> newMap[typeface.fontStyle] = typeface }
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FontFamily

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
         * Creates a new [FontFamily] instance, comprised of the provided [typefaces].
         *
         * All provided [Typeface]s **must** have the same [Typeface.familyName], or
         * this function will throw an exception. No two elements in [typefaces] are
         * allowed to represent the same [FontStyle], because a family can only contain
         * one typeface for each style.
         */
        internal fun fromTypefaces(vararg typefaces: Typeface): FontFamily {
            val expectedFamilyName = typefaces.firstOrNull()?.familyName
                ?: return FontFamily(emptyMap())

            val map = HashMap<FontStyle, Typeface>(typefaces.size)
            for (typeface in typefaces) {
                if (typeface.familyName != expectedFamilyName) {
                    error(
                        "The first typeface has family name of '$expectedFamilyName', but not all provided " +
                                "typefaces are consistent with it (found: '${typeface.familyName}')"
                    )
                }

                if (typeface.fontStyle in map.keys) {
                    error("Trying to add a typeface for style ${typeface.fontStyle}, but it already exists.")
                }

                map[typeface.fontStyle] = typeface
            }
            return FontFamily(map)
        }
    }
}
