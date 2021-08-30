package org.jetbrains.skija.paragraph

import org.jetbrains.annotations.ApiStatus

enum class Affinity {
    UPSTREAM, DOWNSTREAM;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}