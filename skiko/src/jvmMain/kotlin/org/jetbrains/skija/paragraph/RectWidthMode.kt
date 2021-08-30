package org.jetbrains.skija.paragraph

import org.jetbrains.annotations.ApiStatus

enum class RectWidthMode {
    /** Provide tight bounding boxes that fit widths to the runs of each line independently.  */
    TIGHT,

    /** Extends the width of the last rect of each line to match the position of the widest rect over all the lines.  */
    MAX;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}