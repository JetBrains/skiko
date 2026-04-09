package org.jetbrains.skia

import kotlin.jvm.JvmInline

@JvmInline
value class EncodedImageFormat internal constructor(val ordinal: Int) {
    companion object {
        val BMP = EncodedImageFormat(0)
        val GIF = EncodedImageFormat(1)
        val ICO = EncodedImageFormat(2)
        val JPEG = EncodedImageFormat(3)
        val PNG = EncodedImageFormat(4)
        val WBMP = EncodedImageFormat(5)
        val WEBP = EncodedImageFormat(6)
        val PKM = EncodedImageFormat(7)
        val KTX = EncodedImageFormat(8)
        val ASTC = EncodedImageFormat(9)
        val DNG = EncodedImageFormat(10)
        val HEIF = EncodedImageFormat(11)
    }
}
