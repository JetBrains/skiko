package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class FilterTileMode internal constructor(val ordinal: Int) {
    companion object {
        /** Replicate the edge color if the shader draws outside of its original bounds.  */
        val CLAMP = FilterTileMode(0)

        /** Repeat the shader's image horizontally and vertically.  */
        val REPEAT = FilterTileMode(1)

        /** Repeat the shader's image horizontally and vertically, alternating mirror images so that adjacent images always seam.  */
        val MIRROR = FilterTileMode(2)

        /** Only draw within the original domain, return transparent-black everywhere else.  */
        val DECAL = FilterTileMode(3)
    }
}
