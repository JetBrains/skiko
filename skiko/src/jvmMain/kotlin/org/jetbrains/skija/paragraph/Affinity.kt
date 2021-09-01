package org.jetbrains.skija.paragraph

enum class Affinity {
    UPSTREAM, DOWNSTREAM;

    companion object {
        internal val _values = values()
    }
}