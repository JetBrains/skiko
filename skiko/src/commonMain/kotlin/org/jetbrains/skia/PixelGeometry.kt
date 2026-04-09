package org.jetbrains.skia

import kotlin.jvm.JvmInline

// The order and values must be aligned with SkPixelGeometry from the C++ side
@JvmInline
value class PixelGeometry internal constructor(val ordinal: Int) {
    companion object {
        val UNKNOWN = PixelGeometry(0)
        val RGB_H = PixelGeometry(1)
        val BGR_H = PixelGeometry(2)
        val RGB_V = PixelGeometry(3)
        val BGR_V = PixelGeometry(4)
    }
}