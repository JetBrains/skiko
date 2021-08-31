package org.jetbrains.skija

enum class PaintMode {
    FILL, STROKE, STROKE_AND_FILL;

    companion object {
        internal val _values = values()
    }
}