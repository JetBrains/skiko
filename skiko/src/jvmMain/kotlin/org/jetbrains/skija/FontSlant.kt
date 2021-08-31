package org.jetbrains.skija

enum class FontSlant {
    UPRIGHT, ITALIC, OBLIQUE;

    companion object {
        internal val _values = values()
    }
}