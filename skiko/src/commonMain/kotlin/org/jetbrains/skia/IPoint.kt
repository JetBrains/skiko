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

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is IPoint) return false
        if (x != other.x) return false
        return y == other.y
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
