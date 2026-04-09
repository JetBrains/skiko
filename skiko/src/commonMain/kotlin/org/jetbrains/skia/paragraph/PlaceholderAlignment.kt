package org.jetbrains.skia.paragraph

import kotlin.jvm.JvmInline

/**
 * Where to vertically align the placeholder relative to the surrounding text.
 */
@JvmInline
value class PlaceholderAlignment internal constructor(val ordinal: Int) {
    companion object {
        /**
         * Match the baseline of the placeholder with the baseline.
         */
        val BASELINE = PlaceholderAlignment(0)
        /**
         * Align the bottom edge of the placeholder with the baseline such that the
         * placeholder sits on top of the baseline.
         */
        val ABOVE_BASELINE = PlaceholderAlignment(1)
        /**
         * Align the top edge of the placeholder with the baseline specified in
         * such that the placeholder hangs below the baseline.
         */
        val BELOW_BASELINE = PlaceholderAlignment(2)
        /**
         * Align the top edge of the placeholder with the top edge of the font.
         * When the placeholder is very tall, the extra space will hang from
         * the top and extend through the bottom of the line.
         */
        val TOP = PlaceholderAlignment(3)
        /**
         * Align the bottom edge of the placeholder with the top edge of the font.
         * When the placeholder is very tall, the extra space will rise from
         * the bottom and extend through the top of the line.
         */
        val BOTTOM = PlaceholderAlignment(4)
        /**
         * Align the middle of the placeholder with the middle of the text. When the
         * placeholder is very tall, the extra space will grow equally from
         * the top and bottom of the line.
         */
        val MIDDLE = PlaceholderAlignment(5)
    }
}
