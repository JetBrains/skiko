package org.jetbrains.skia

class IPoint(val x: Int, val y: Int) {

    fun offset(dx: Int, dy: Int): IPoint {
        return IPoint(x + dx, y + dy)
    }

    fun offset(vec: IPoint): IPoint {
        return offset(vec.x, vec.y)
    }

    val isEmpty: Boolean
        get() = x <= 0 || y <= 0

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is IPoint) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (x != other.x) return false
        return y == other.y
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is IPoint
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + x
        result = result * PRIME + y
        return result
    }

    override fun toString(): String {
        return "IPoint(_x=$x, _y=$y)"
    }

    companion object {
        val ZERO = IPoint(0, 0)
    }
}

fun toIPoint(p: Long): IPoint = IPoint((p ushr 32).toInt(), (p and -1).toInt())
