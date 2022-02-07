package org.jetbrains.skia

/**
 *
 * This specifies how the next frame is based on this frame.
 *
 *
 * Names are based on the GIF 89a spec.
 */
enum class AnimationDisposalMode {
    UNUSED,

    /**
     *
     * The next frame should be drawn on top of this one.
     *
     *
     * In a GIF, a value of 0 (not specified) is also treated as KEEP.
     */
    KEEP,

    /**
     *
     * Similar to KEEP, except the area inside this frame's rectangle
     * should be cleared to the BackGround color (transparent) before
     * drawing the next frame.
     */
    RESTORE_BG_COLOR,

    /**
     *
     * The next frame should be drawn on top of the previous frame - i.e.
     * disregarding this one.
     *
     *
     * In a GIF, a value of 4 is also treated as RestorePrevious.
     */
    RESTORE_PREVIOUS;
}