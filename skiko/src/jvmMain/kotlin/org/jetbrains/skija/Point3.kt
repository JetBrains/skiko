package org.jetbrains.skija

class Point3(val x: Float, val y: Float, val z: Float) {
    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is Point3) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (java.lang.Float.compare(x, other.x) != 0) return false
        if (java.lang.Float.compare(y, other.y) != 0) return false
        return if (java.lang.Float.compare(z, other.z) != 0) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is Point3
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + java.lang.Float.floatToIntBits(x)
        result = result * PRIME + java.lang.Float.floatToIntBits(y)
        result = result * PRIME + java.lang.Float.floatToIntBits(z)
        return result
    }

    override fun toString(): String {
        return "Point3(_x=" + x + ", _y=" + y + ", _z=" + z + ")"
    }
}