package org.jetbrains.skia


class IRange(val start: Int, val end: Int) {

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is IRange) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (start != other.start) return false
        return if (end != other.end) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is IRange
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + start
        result = result * PRIME + end
        return result
    }

    override fun toString(): String {
        return "IRange(_start=" + start + ", _end=" + end + ")"
    }

    companion object {
        internal fun _makeFromLong(l: Long): IRange {
            return IRange((l ushr 32).toInt(), (l and -1).toInt())
        }
    }
}