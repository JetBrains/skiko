package org.jetbrains.skija

enum class ClipMode {
    DIFFERENCE, INTERSECT;

    companion object {
        internal val _values = values()
    }
}