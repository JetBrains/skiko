package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

enum class InversionMode {
    NO, BRIGHTNESS, LIGHTNESS;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}