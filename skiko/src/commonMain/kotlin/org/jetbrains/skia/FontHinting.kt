package org.jetbrains.skia

import kotlin.jvm.JvmInline

/**
 * Level of glyph outline adjustment
 */
@JvmInline
value class FontHinting internal constructor(val ordinal: Int) {
    companion object {
        /**
         * glyph outlines unchanged
         */
        val NONE = FontHinting(0)

        /**
         * minimal modification to improve constrast
         */
        val SLIGHT = FontHinting(1)

        /**
         * glyph outlines modified to improve constrast
         */
        val NORMAL = FontHinting(2)

        /**
         * modifies glyph outlines for maximum constrast
         */
        val FULL = FontHinting(3)
    }
}