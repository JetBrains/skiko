package org.jetbrains.skija


enum class PathEllipseArc {
    /** Smaller of arc pair.  */
    SMALLER,

    /** Larger of arc pair.  */
    LARGER;

    companion object {
        internal val _values = values()
    }
}