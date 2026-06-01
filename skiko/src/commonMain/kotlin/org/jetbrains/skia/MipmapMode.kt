package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class MipmapMode internal constructor(val ordinal: Int) {
    companion object {
        /**
         * ignore mipmap levels, sample from the "base"
         */
        val NONE = MipmapMode(0)

        /**
         * sample from the nearest level
         */
        val NEAREST = MipmapMode(1)

        /**
         * interpolate between the two nearest levels
         */
        val LINEAR = MipmapMode(2)
    }
}
