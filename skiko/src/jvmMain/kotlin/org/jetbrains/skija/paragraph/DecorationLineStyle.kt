package org.jetbrains.skija.paragraph

import org.jetbrains.annotations.ApiStatus

enum class DecorationLineStyle {
    SOLID, DOUBLE, DOTTED, DASHED, WAVY;

    companion object {
        internal val _values = values()
    }
}