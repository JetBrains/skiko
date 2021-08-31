package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

enum class MipmapMode {
    /**
     * ignore mipmap levels, sample from the "base"
     */
    NONE,

    /**
     * sample from the nearest level
     */
    NEAREST,

    /**
     * interpolate between the two nearest levels
     */
    LINEAR;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}