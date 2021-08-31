package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

enum class PaintMode {
    FILL, STROKE, STROKE_AND_FILL;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}