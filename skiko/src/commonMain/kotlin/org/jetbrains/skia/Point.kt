package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.withResult

class Point(val x: Float, val y: Float) {

    fun offset(dx: Float, dy: Float): Point {
        return Point(x + dx, y + dy)
    }

    fun offset(vec: Point): Point {
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

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Point) return false
        if (x.compareTo(other.x) != 0) return false
        return y.compareTo(other.y) == 0
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + x.toBits()
        result = result * PRIME + y.toBits()
        return result
    }

    override fun toString(): String {
        return "Point(_x=$x, _y=$y)"
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
            require(pts.size % 2 == 0) { "Expected " + pts.size + " % 2 == 0" }
            val arr = arrayOfNulls<Point>(pts.size / 2)
            for (i in 0 until pts.size / 2) arr[i] = Point(pts[i * 2], pts[i * 2 + 1])
            return arr
        }

        internal fun fromInteropPointer(block: (InteropPointer) -> Unit): Point {
            val result = withResult(FloatArray(2), block)
            return Point(result[0], result[1])
        }
    }
}