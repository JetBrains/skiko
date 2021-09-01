package org.jetbrains.skija

enum class PathFillMode {
    /** Specifies that "inside" is computed by a non-zero sum of signed edge crossings.  */
    WINDING,

    /** Specifies that "inside" is computed by an odd number of edge crossings.  */
    EVEN_ODD,

    /** Same as [.WINDING], but draws outside of the path, rather than inside.  */
    INVERSE_WINDING,

    /** Same as [.EVEN_ODD], but draws outside of the path, rather than inside.  */
    INVERSE_EVEN_ODD;

    /**
     * Returns if FillType describes area outside Path geometry. The inverse fill area
     * extends indefinitely.
     *
     * @return  true if FillType is [.INVERSE_WINDING] or [.INVERSE_EVEN_ODD]
     */
    val isInverse: Boolean
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
            else -> throw RuntimeException("Unreachable")
        }
    }
}