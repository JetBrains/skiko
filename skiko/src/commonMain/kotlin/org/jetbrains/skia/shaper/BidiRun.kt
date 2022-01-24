package org.jetbrains.skia.shaper

class BidiRun(
    val end: Int,
    /**
     * The unicode bidi embedding level (even ltr, odd rtl)
     */
    val level: Int
) {
    /**
     * The unicode bidi embedding level (even ltr, odd rtl)
     */
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is BidiRun) return false
        if (end != other.end) return false
        return level == other.level
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + end
        result = result * PRIME + level
        return result
    }

    override fun toString(): String {
        return "BidiRun(_end=$end, _level=$level)"
    }
}