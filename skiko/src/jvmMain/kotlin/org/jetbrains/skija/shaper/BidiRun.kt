package org.jetbrains.skija.shaper

class BidiRun(
    internal val end: Int,
    /**
     * The unicode bidi embedding level (even ltr, odd rtl)
     */
    internal val level: Int
) {
    /**
     * The unicode bidi embedding level (even ltr, odd rtl)
     */
    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is BidiRun) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (end != other.end) return false
        return if (level != other.level) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is BidiRun
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + end
        result = result * PRIME + level
        return result
    }

    override fun toString(): String {
        return "BidiRun(_end=" + end + ", _level=" + level + ")"
    }
}