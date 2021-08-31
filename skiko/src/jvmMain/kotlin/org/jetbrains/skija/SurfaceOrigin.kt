package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

enum class SurfaceOrigin {
    TOP_LEFT, BOTTOM_LEFT;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}