package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

enum class ColorChannel {
    R, G, B, A;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}