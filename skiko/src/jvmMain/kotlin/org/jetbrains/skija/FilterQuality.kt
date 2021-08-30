package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

enum class FilterQuality {
    /** fastest but lowest quality, typically nearest-neighbor  */
    NONE,

    /** typically bilerp  */
    LOW,

    /** typically bilerp + mipmaps for down-scaling  */
    MEDIUM,

    /** slowest but highest quality, typically bicubic or bett  */
    HIGH;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}