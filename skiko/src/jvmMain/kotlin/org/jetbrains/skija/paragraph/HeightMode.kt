package org.jetbrains.skija.paragraph

import org.jetbrains.annotations.ApiStatus

enum class HeightMode {
    ALL, DISABLE_FIRST_ASCENT, DISABLE_LAST_DESCENT, DISABLE_ALL;

    companion object {
        internal val _values = values()
    }
}