package org.jetbrains.skia

class IRect internal constructor(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int
        get() = right - left
    val height: Int
        get() = bottom - top

    fun intersect(other: IRect): IRect? {
        return if (right <= other.left || other.right <= left || bottom <= other.top || other.bottom <= top) null else IRect(
            Math.max(left, other.left), Math.max(top, other.top), Math.min(
                right, other.right
            ), Math.min(bottom, other.bottom)
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

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is IRect) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (left != other.left) return false
        if (top != other.top) return false
        if (right != other.right) return false
        return if (bottom != other.bottom) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is IRect
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
        return "IRect(_left=" + left + ", _top=" + top + ", _right=" + right + ", _bottom=" + bottom + ")"
    }

    companion object {
        @JvmStatic
        fun makeLTRB(l: Int, t: Int, r: Int, b: Int): IRect {
            require(l <= r) { "IRect::makeLTRB expected l <= r, got $l > $r" }
            require(t <= b) { "IRect::makeLTRB expected t <= b, got $t > $b" }
            return IRect(l, t, r, b)
        }

        @JvmStatic
        fun makeXYWH(l: Int, t: Int, w: Int, h: Int): IRect {
            require(w >= 0) { "IRect::makeXYWH expected w >= 0, got: $w" }
            require(h >= 0) { "IRect::makeXYWH expected h >= 0, got: $h" }
            return if (w >= 0 && h >= 0) IRect(l, t, l + w, t + h) else throw IllegalArgumentException()
        }

        @JvmStatic
        fun makeWH(w: Int, h: Int): IRect {
            require(w >= 0) { "IRect::makeWH expected w >= 0, got: $w" }
            require(h >= 0) { "IRect::makeWH expected h >= 0, got: $h" }
            return if (w >= 0 && h >= 0) IRect(0, 0, w, h) else throw IllegalArgumentException()
        }
    }
}