package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class PathFillMode internal constructor(val ordinal: Int) {
    /**
     * Returns if FillType describes area outside Path geometry. The inverse fill area
     * extends indefinitely.
     *
     * @return  true if FillType is [.INVERSE_WINDING] or [.INVERSE_EVEN_ODD]
     */
    inline val isInverse: Boolean
        get() = this == INVERSE_WINDING || this == INVERSE_EVEN_ODD


    /**
     * Returns the inverse fill type. The inverse of FillType describes the area
     * unmodified by the original FillType.
     *
     * @return  inverse FillType
     */
    fun inverse(): PathFillMode {
        return when (this) {
            WINDING -> INVERSE_WINDING
            EVEN_ODD -> INVERSE_EVEN_ODD
            INVERSE_WINDING -> WINDING
            INVERSE_EVEN_ODD -> EVEN_ODD
            else -> throw IllegalStateException("Unsupported PathFillMode: $this")
        }

    }


    companion object {
        /** Specifies that "inside" is computed by a non-zero sum of signed edge crossings.  */
        val WINDING = PathFillMode(0)


        /** Specifies that "inside" is computed by an odd number of edge crossings.  */
        val EVEN_ODD = PathFillMode(1)

        /** Same as [.WINDING], but draws outside of the path, rather than inside.  */
        val INVERSE_WINDING = PathFillMode(2)

        /** Same as [.EVEN_ODD], but draws outside of the path, rather than inside.  */
        val INVERSE_EVEN_ODD = PathFillMode(3)
    }
}
