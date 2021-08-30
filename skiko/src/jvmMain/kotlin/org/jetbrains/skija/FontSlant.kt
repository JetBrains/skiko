package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

enum class FontSlant {
    UPRIGHT, ITALIC, OBLIQUE;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}