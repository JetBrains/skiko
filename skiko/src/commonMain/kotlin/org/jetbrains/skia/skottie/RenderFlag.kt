package org.jetbrains.skia.skottie

enum class RenderFlag(internal val _flag: Int) {
    /**
     * When rendering into a known transparent buffer, clients can pass
     * this flag to avoid some unnecessary compositing overhead for
     * animations using layer blend modes.
     */
    SKIP_TOP_LEVEL_ISOLATION(0x01),

    /**
     * By default, content is clipped to the intrinsic animation
     * bounds (as determined by its size).  If this flag is set,
     * then the animation can draw outside of the bounds.
     */
    DISABLE_TOP_LEVEL_CLIPPING(0x02);
}