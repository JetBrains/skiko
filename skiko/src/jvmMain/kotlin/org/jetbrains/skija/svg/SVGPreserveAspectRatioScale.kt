package org.jetbrains.skija.svg

import org.jetbrains.annotations.ApiStatus

enum class SVGPreserveAspectRatioScale {
    MEET, SLICE;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}