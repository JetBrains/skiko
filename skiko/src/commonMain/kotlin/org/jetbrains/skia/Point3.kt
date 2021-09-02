package org.jetbrains.skia

class Point3(val x: Float, val y: Float, val z: Float) {
    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is Point3) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (x.compareTo(other.x) != 0) return false
        if (y.compareTo(other.y) != 0) return false
        return z.compareTo(other.z) == 0
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is Point3
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + x.toBits()
        result = result * PRIME + y.toBits()
        result = result * PRIME + z.toBits()
        return result
    }

    override fun toString(): String {
        return "Point3(_x=$x, _y=$y, _z=$z)"
    }
}