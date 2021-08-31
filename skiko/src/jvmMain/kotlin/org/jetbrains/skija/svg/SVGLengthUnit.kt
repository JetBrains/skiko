package org.jetbrains.skija.svg

enum class SVGLengthUnit {
    UNKNOWN, NUMBER, PERCENTAGE, EMS, EXS, PX, CM, MM, IN, PT, PC;

    companion object {
        internal val _values = values()
    }
}