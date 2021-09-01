package org.jetbrains.skija.paragraph

enum class Alignment {
    LEFT, RIGHT, CENTER, JUSTIFY, START, END;

    companion object {
        internal val _values = values()
    }
}