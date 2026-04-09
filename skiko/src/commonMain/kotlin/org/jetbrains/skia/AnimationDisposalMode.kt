package org.jetbrains.skia

import kotlin.jvm.JvmInline

/**
 *
 * This specifies how the next frame is based on this frame.
 *
 *
 * Names are based on the GIF 89a spec.
 */
@JvmInline
value class AnimationDisposalMode internal constructor(val ordinal: Int) {
    companion object {
        /**
         * Do not use
         */
        val UNUSED = AnimationDisposalMode(0)

        /**
         *
         * The next frame should be drawn on top of this one.
         *
         *
         * In a GIF, a value of 0 (not specified) is also treated as KEEP.
         */
        val KEEP = AnimationDisposalMode(1)

        /**
         *
         * Similar to KEEP, except the area inside this frame's rectangle
         * should be cleared to the BackGround color (transparent) before
         * drawing the next frame.
         */
        val RESTORE_BG_COLOR = AnimationDisposalMode(2)

        /**
         *
         * The next frame should be drawn on top of the previous frame - i.e.
         * disregarding this one.
         *
         *
         * In a GIF, a value of 4 is also treated as RestorePrevious.
         */
        val RESTORE_PREVIOUS = AnimationDisposalMode(3)
    }
}