package org.jetbrains.skia.encode

import org.jetbrains.skia.Data
import org.jetbrains.skia.Image

/** Encodes an image, returning null if encoding fails. */
interface ImageEncoder {
    fun encode(image: Image): Data?
}
