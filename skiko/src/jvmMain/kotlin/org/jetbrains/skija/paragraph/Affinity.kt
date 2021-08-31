package org.jetbrains.skija.paragraph

import org.jetbrains.annotations.ApiStatus

enum class Affinity {
    UPSTREAM, DOWNSTREAM;

    companion object {
        internal val _values = values()
    }
}