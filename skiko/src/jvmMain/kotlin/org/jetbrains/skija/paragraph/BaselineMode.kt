package org.jetbrains.skija.paragraph

import org.jetbrains.annotations.ApiStatus

enum class BaselineMode {
    ALPHABETIC, IDEOGRAPHIC;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}