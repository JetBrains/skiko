package org.jetbrains.skija.paragraph

enum class HeightMode {
    ALL, DISABLE_FIRST_ASCENT, DISABLE_LAST_DESCENT, DISABLE_ALL;

    companion object {
        internal val _values = values()
    }
}