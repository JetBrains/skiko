package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.withNullableResult
import org.jetbrains.skia.impl.withResult
import org.jetbrains.skiko.InternalSkikoApi
import org.jetbrains.skiko.internal.UnspecifiedPackedFloats
import org.jetbrains.skiko.internal.packFloats
import org.jetbrains.skiko.internal.unpackFloat1
import org.jetbrains.skiko.internal.unpackFloat2

inline fun Point(x: Float, y: Float): Point = Point(packFloats(x, y))

@kotlin.jvm.JvmInline
value class Point(val packedValue: Long) {
    inline val x: Float
        get() = unpackFloat1(packedValue)

    inline val y: Float
        get() = unpackFloat2(packedValue)

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

    override fun toString(): String {
        return "Point(x=$x, y=$y)"
    }

    companion object {
        val ZERO: Point
            get() = Point(0x0L)

        val Unspecified: Point
            get() = Point(UnspecifiedPackedFloats)

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

        @InternalSkikoApi
        fun fromInteropPointer(block: InteropScope.(InteropPointer) -> Unit): Point {
            val result = withResult(FloatArray(2), block)
            return Point(result[0], result[1])
        }

        @InternalSkikoApi
        fun fromNullableInteropPointer(block: InteropScope.(InteropPointer) -> Boolean): Point? {
            val result = withNullableResult(FloatArray(2), block) ?: return null
            return Point(result[0], result[1])
        }
    }
}
