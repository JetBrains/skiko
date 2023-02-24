package org.jetbrains.skia

// The order and values must be aligned with SkPixelGeometry from the C++ side
enum class PixelGeometry {
    UNKNOWN,
    RGB_H,
    BGR_H,
    RGB_V,
    BGR_V;
}