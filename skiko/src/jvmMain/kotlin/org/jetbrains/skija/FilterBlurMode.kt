package org.jetbrains.skija

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
        internal val _values = values()
    }
}