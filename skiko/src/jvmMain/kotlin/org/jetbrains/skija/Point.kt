package org.jetbrains.skija

class Point(val x: Float, val y: Float) {

    fun offset(dx: Float, dy: Float): Point {
        return Point(x + dx, y + dy)
    }

    fun offset(vec: Point): Point {
        assert(vec != null) { "Point::offset expected other != null" }
        return offset(vec.x, vec.y)
    }

    fun scale(scale: Float): Point {
        return scale(scale, scale)
    }

    fun scale(sx: Float, sy: Float): Point {
        return Point(x * sx, y * sy)
    }

    val isEmpty: Boolean
        get() = x <= 0 || y <= 0

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is Point) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (java.lang.Float.compare(x, other.x) != 0) return false
        return if (java.lang.Float.compare(y, other.y) != 0) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is Point
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + java.lang.Float.floatToIntBits(x)
        result = result * PRIME + java.lang.Float.floatToIntBits(y)
        return result
    }

    override fun toString(): String {
        return "Point(_x=" + x + ", _y=" + y + ")"
    }

    companion object {
        val ZERO = Point(0f, 0f)
        fun flattenArray(pts: Array<Point>?): FloatArray? {
            if (pts == null) return null
            val arr = FloatArray(pts.size * 2)
            for (i in pts.indices) {
                arr[i * 2] = pts[i].x
                arr[i * 2 + 1] = pts[i].y
            }
            return arr
        }

        fun fromArray(pts: FloatArray?): Array<Point?>? {
            if (pts == null) return null
            assert(pts.size % 2 == 0) { "Expected " + pts.size + " % 2 == 0" }
            val arr = arrayOfNulls<Point>(pts.size / 2)
            for (i in 0 until pts.size / 2) arr[i] = Point(pts[i * 2], pts[i * 2 + 1])
            return arr
        }
    }
}