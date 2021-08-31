package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

enum class PixelGeometry {
    UNKNOWN, RGB_H, BGR_H, RGB_V, BGR_V;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}