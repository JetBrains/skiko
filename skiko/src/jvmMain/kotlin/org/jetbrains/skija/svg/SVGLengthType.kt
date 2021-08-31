package org.jetbrains.skija.svg

import org.jetbrains.annotations.ApiStatus

enum class SVGLengthType {
    HORIZONTAL, VERTICAL, OTHER;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}