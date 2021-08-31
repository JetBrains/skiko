package org.jetbrains.skija.paragraph

import org.jetbrains.annotations.ApiStatus

enum class DecorationLineStyle {
    SOLID, DOUBLE, DOTTED, DASHED, WAVY;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}