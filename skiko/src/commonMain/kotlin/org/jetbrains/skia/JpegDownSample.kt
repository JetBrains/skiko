package org.jetbrains.skia

enum class JpegDownSample {
    /**
     * Reduction by a factor of two in both the horizontal and vertical directions.
     */
    K420,

    /**
     * Reduction by a factor of two in the horizontal direction.
     */
    K422,

    /**
     * No downsampling.
     */
    K444,
}
