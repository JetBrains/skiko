package org.jetbrains.skia

import kotlin.jvm.JvmInline

/**
 *
 * Describes how to interpret the alpha component of a pixel. A pixel may
 * be opaque, or alpha, describing multiple levels of transparency.
 *
 *
 * In simple blending, alpha weights the draw color and the destination
 * color to create a new color. If alpha describes a weight from zero to one:
 *
 * <pre>`new color = draw color * alpha + destination color * (1 - alpha)`</pre>
 *
 *
 * In practice alpha is encoded in two or more bits, where 1.0 equals all bits set.
 *
 *
 * RGB may have alpha included in each component value; the stored
 * value is the original RGB multiplied by alpha. Premultiplied color
 * components improve performance.
 */
@JvmInline
value class ColorAlphaType internal constructor(val ordinal: Int) {
    companion object {
        /**
         * uninitialized
         */
        val UNKNOWN = ColorAlphaType(0)

        /**
         * pixel is opaque
         */
        val OPAQUE = ColorAlphaType(1)

        /**
         * pixel components are premultiplied by alpha
         */
        val PREMUL = ColorAlphaType(2)

        /**
         * pixel components are independent of alpha
         */
        val UNPREMUL = ColorAlphaType(3)
    }
}