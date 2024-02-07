package org.jetbrains.skia

import kotlin.jvm.JvmStatic

class IRect internal constructor(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int
        get() = right - left
    val height: Int
        get() = bottom - top

    fun intersect(other: IRect): IRect? {
        return if (right <= other.left || other.right <= left ||
                bottom <= other.top || other.bottom <= top) null
            else IRect(
                maxOf(left, other.left), maxOf(top, other.top),
                minOf(right, other.right), minOf(bottom, other.bottom)
            )
    }

    fun offset(dx: Int, dy: Int): IRect {
        return IRect(left + dx, top + dy, right + dx, bottom + dy)
    }

    fun offset(vec: IPoint): IRect {
        return offset(vec.x, vec.y)
    }

    fun toRect(): Rect {
        return Rect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
    }

    // This is a helper to pass IRect instance through interop border
    internal fun serializeToIntArray(): IntArray {
        return intArrayOf(left, top, right, bottom)
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is IRect) return false
        if (left != other.left) return false
        if (top != other.top) return false
        if (right != other.right) return false
        return bottom == other.bottom
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + left
        result = result * PRIME + top
        result = result * PRIME + right
        result = result * PRIME + bottom
        return result
    }

    override fun toString(): String {
        return "IRect(_left=$left, _top=$top, _right=$right, _bottom=$bottom)"
    }

    companion object {
        @JvmStatic
        fun makeLTRB(l: Int, t: Int, r: Int, b: Int): IRect {
            return IRect(l, t, r, b)
        }

        @JvmStatic
        fun makeXYWH(l: Int, t: Int, w: Int, h: Int): IRect {
            return IRect(l, t, l + w, t + h)
        }

        @JvmStatic
        fun makeWH(w: Int, h: Int): IRect {
            return IRect(0, 0, w, h)
        }
    }
}
