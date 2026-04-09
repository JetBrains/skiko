package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class FilterMode internal constructor(val ordinal: Int) {
    companion object {
        /**
         * single sample point (nearest neighbor)
         */
        val NEAREST = FilterMode(0)

        /**
         * interporate between 2x2 sample points (bilinear interpolation)
         */
        val LINEAR = FilterMode(1)
    }
}
