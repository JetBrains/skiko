package org.jetbrains.skija

enum class PathDirection {
    /** Clockwise direction for adding closed contours.  */
    CLOCKWISE,

    /** Counter-clockwise direction for adding closed contours.  */
    COUNTER_CLOCKWISE;

    companion object {
        internal val _values = values()
    }
}