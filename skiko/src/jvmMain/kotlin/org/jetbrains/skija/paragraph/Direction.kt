package org.jetbrains.skija.paragraph

import org.jetbrains.annotations.ApiStatus

enum class Direction {
    RTL, LTR;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}