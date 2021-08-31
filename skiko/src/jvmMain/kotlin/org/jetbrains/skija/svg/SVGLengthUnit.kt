package org.jetbrains.skija.svg

import org.jetbrains.annotations.ApiStatus

enum class SVGLengthUnit {
    UNKNOWN, NUMBER, PERCENTAGE, EMS, EXS, PX, CM, MM, IN, PT, PC;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}