package org.jetbrains.skija

enum class PixelGeometry {
    UNKNOWN, RGB_H, BGR_H, RGB_V, BGR_V;

    companion object {
        internal val _values = values()
    }
}