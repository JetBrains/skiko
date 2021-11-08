package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.withResult
import kotlin.jvm.JvmStatic

open class Rect constructor(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float
        get() = right - left
    val height: Float
        get() = bottom - top

    fun intersect(other: Rect): Rect? {
        return if (right <= other.left || other.right <= left || bottom <= other.top || other.bottom <= top) null else Rect(
            maxOf(
                left, other.left
            ), maxOf(top, other.top), minOf(
                right, other.right
            ), minOf(bottom, other.bottom)
        )
    }

    fun scale(scale: Float): Rect {
        return scale(scale, scale)
    }

    fun scale(sx: Float, sy: Float): Rect {
        return Rect(left * sx, top * sy, right * sx, bottom * sy)
    }

    fun offset(dx: Float, dy: Float): Rect {
        return Rect(left + dx, top + dy, right + dx, bottom + dy)
    }

    fun offset(vec: Point): Rect {
        return offset(vec.x, vec.y)
    }

    fun toIRect(): IRect {
        return IRect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    open fun inflate(spread: Float): Rect {
        return if (spread <= 0) makeLTRB(
            left - spread, top - spread, maxOf(
                left - spread, right + spread
            ), maxOf(top - spread, bottom + spread)
        ) else RRect.makeLTRB(
            left - spread, top - spread, maxOf(left - spread, right + spread), maxOf(
                top - spread, bottom + spread
            ), spread
        )
    }

    val isEmpty: Boolean
        get() = right == left || top == bottom

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Rect) return false
        if (left.compareTo(other.left) != 0) return false
        if (top.compareTo(other.top) != 0) return false
        if (right.compareTo(other.right) != 0) return false
        return bottom.compareTo(other.bottom) == 0
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + left.toBits()
        result = result * PRIME + top.toBits()
        result = result * PRIME + right.toBits()
        result = result * PRIME + bottom.toBits()
        return result
    }

    override fun toString(): String {
        return "Rect(_left=$left, _top=$top, _right=$right, _bottom=$bottom)"
    }

    companion object {
        @JvmStatic
        fun makeLTRB(l: Float, t: Float, r: Float, b: Float): Rect {
            require(l <= r) { "Rect::makeLTRB expected l <= r, got $l > $r" }
            require(t <= b) { "Rect::makeLTRB expected t <= b, got $t > $b" }
            return Rect(l, t, r, b)
        }

        @JvmStatic
        fun makeWH(w: Float, h: Float): Rect {
            require(w >= 0) { "Rect::makeWH expected w >= 0, got: $w" }
            require(h >= 0) { "Rect::makeWH expected h >= 0, got: $h" }
            return Rect(0f, 0f, w, h)
        }

        @JvmStatic
        fun makeWH(size: Point): Rect {
            return makeWH(size.x, size.y)
        }

        @JvmStatic
        fun makeXYWH(l: Float, t: Float, w: Float, h: Float): Rect {
            require(w >= 0) { "Rect::makeXYWH expected w >= 0, got: $w" }
            require(h >= 0) { "Rect::makeXYWH expected h >= 0, got: $h" }
            return Rect(l, t, l + w, t + h)
        }

        internal fun fromInteropPointer(block: InteropScope.(InteropPointer) -> Unit): Rect {
            val result = withResult(FloatArray(4), block)
            return Rect(result[0], result[1], result[2], result[3])
        }

        internal fun fromInteropPointerNullable(block: (InteropPointer) -> Boolean): Rect? {
            var result = true
            val rect = fromInteropPointer { result = block(it) }
            return if (result) { rect } else { null }
        }
    }
}