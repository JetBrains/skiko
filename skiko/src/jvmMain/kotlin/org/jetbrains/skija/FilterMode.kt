package org.jetbrains.skija

enum class FilterMode {
    /**
     * single sample point (nearest neighbor)
     */
    NEAREST,

    /**
     * interporate between 2x2 sample points (bilinear interpolation)
     */
    LINEAR;

    companion object {
        internal val _values = values()
    }
}