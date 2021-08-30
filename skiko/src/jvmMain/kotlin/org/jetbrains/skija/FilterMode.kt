package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

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
        @ApiStatus.Internal
        val _values = values()
    }
}