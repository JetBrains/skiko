package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

enum class FilterBlurMode {
    /** fuzzy inside and outside  */
    NORMAL,

    /** solid inside, fuzzy outside  */
    SOLID,

    /** nothing inside, fuzzy outside  */
    OUTER,

    /** fuzzy inside, nothing outside  */
    INNER;

    companion object {
        @ApiStatus.Internal
        val _values = values()
    }
}