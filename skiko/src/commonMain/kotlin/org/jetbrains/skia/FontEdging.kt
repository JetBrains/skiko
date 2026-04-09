package org.jetbrains.skia

import kotlin.jvm.JvmInline

/**
 * Whether edge pixels draw opaque or with partial transparency.
 */
@JvmInline
value class FontEdging internal constructor(val ordinal: Int) {
    companion object {
        /**
         * no transparent pixels on glyph edges
         */
        val ALIAS = FontEdging(0)

        /**
         * may have transparent pixels on glyph edges
         */
        val ANTI_ALIAS = FontEdging(1)

        /**
         * glyph positioned in pixel using transparency
         */
        val SUBPIXEL_ANTI_ALIAS = FontEdging(2)
    }
}
