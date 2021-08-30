package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

enum class EncodedImageFormat {
    BMP, GIF, ICO, JPEG, PNG, WBMP, WEBP, PKM, KTX, ASTC, DNG, HEIF;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}