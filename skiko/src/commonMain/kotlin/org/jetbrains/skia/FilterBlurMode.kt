package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class FilterBlurMode internal constructor(val ordinal: Int) {
    companion object {
        /** fuzzy inside and outside  */
        val NORMAL = FilterBlurMode(0)

        /** solid inside, fuzzy outside  */
        val SOLID = FilterBlurMode(1)

        /** nothing inside, fuzzy outside  */
        val OUTER = FilterBlurMode(2)

        /** fuzzy inside, nothing outside  */
        val INNER = FilterBlurMode(3)
    }
}