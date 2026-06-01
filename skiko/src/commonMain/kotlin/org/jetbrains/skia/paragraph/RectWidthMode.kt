package org.jetbrains.skia.paragraph

import kotlin.jvm.JvmInline

@JvmInline
value class RectWidthMode internal constructor(val ordinal: Int) {
    companion object {
        /** Provide tight bounding boxes that fit widths to the runs of each line independently.  */
        val TIGHT = RectWidthMode(0)
        /** Extends the width of the last rect of each line to match the position of the widest rect over all the lines.  */
        val MAX = RectWidthMode(1)
    }
}
