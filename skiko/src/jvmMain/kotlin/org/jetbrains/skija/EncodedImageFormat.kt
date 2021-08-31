package org.jetbrains.skija

enum class EncodedImageFormat {
    BMP, GIF, ICO, JPEG, PNG, WBMP, WEBP, PKM, KTX, ASTC, DNG, HEIF;

    companion object {
        internal val _values = values()
    }
}