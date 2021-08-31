package org.jetbrains.skija

enum class ContentChangeMode {
    /** Discards surface on change.  */
    DISCARD,

    /** Preserves surface on change.  */
    RETAIN;

    companion object {
        internal val _values = values()
    }
}