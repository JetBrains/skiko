package org.jetbrains.skija.paragraph

import org.jetbrains.annotations.ApiStatus

enum class Direction {
    RTL, LTR;

    companion object {
        internal val _values = values()
    }
}