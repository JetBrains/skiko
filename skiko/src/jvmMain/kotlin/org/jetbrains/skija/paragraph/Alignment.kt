package org.jetbrains.skija.paragraph

import org.jetbrains.annotations.ApiStatus

enum class Alignment {
    LEFT, RIGHT, CENTER, JUSTIFY, START, END;

    companion object {
        internal val _values = values()
    }
}