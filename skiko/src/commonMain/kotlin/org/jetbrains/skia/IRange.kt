package org.jetbrains.skia

class IRange(val start: Int, val end: Int) {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is IRange) return false
        if (start != other.start) return false
        return end == other.end
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + start
        result = result * PRIME + end
        return result
    }

    override fun toString(): String {
        return "IRange(_start=$start, _end=$end)"
    }
}