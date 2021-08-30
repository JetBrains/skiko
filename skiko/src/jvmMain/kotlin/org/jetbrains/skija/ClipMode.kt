package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

enum class ClipMode {
    DIFFERENCE, INTERSECT;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}