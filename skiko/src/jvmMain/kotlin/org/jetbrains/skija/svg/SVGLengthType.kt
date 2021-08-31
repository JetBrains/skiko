package org.jetbrains.skija.svg

enum class SVGLengthType {
    HORIZONTAL, VERTICAL, OTHER;

    companion object {
        internal val _values = values()
    }
}