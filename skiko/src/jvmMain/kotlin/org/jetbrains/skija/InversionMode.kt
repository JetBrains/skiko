package org.jetbrains.skija

enum class InversionMode {
    NO, BRIGHTNESS, LIGHTNESS;

    companion object {
        internal val _values = values()
    }
}