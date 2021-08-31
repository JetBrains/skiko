package org.jetbrains.skija

enum class SurfaceOrigin {
    TOP_LEFT, BOTTOM_LEFT;

    companion object {
        internal val _values = values()
    }
}