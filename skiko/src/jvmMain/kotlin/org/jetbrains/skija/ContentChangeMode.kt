package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

enum class ContentChangeMode {
    /** Discards surface on change.  */
    DISCARD,

    /** Preserves surface on change.  */
    RETAIN;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}