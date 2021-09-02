package org.jetbrains.skia.paragraph

import org.jetbrains.skia.Point

class Shadow(val color: Int, val offsetX: Float, val offsetY: Float, val blurSigma: Double) {

    constructor(color: Int, offset: Point, blurSigma: Double) : this(color, offset.x, offset.y, blurSigma) {}

    val offset: Point
        get() = Point(offsetX, offsetY)

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is Shadow) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (color != other.color) return false
        if (java.lang.Float.compare(offsetX, other.offsetX) != 0) return false
        if (java.lang.Float.compare(offsetY, other.offsetY) != 0) return false
        return if (java.lang.Double.compare(blurSigma, other.blurSigma) != 0) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is Shadow
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + color
        result = result * PRIME + java.lang.Float.floatToIntBits(offsetX)
        result = result * PRIME + java.lang.Float.floatToIntBits(offsetY)
        val `$_blurSigma` = java.lang.Double.doubleToLongBits(blurSigma)
        result = result * PRIME + (`$_blurSigma` ushr 32 xor `$_blurSigma`).toInt()
        return result
    }

    override fun toString(): String {
        return "Shadow(_color=" + color + ", _offsetX=" + offsetX + ", _offsetY=" + offsetY + ", _blurSigma=" + blurSigma + ")"
    }

    fun withColor(_color: Int): Shadow {
        return if (color == _color) this else Shadow(_color, offsetX, offsetY, blurSigma)
    }

    fun withOffsetX(_offsetX: Float): Shadow {
        return if (offsetX == _offsetX) this else Shadow(color, _offsetX, offsetY, blurSigma)
    }

    fun withOffsetY(_offsetY: Float): Shadow {
        return if (offsetY == _offsetY) this else Shadow(color, offsetX, _offsetY, blurSigma)
    }

    fun withBlurSigma(_blurSigma: Double): Shadow {
        return if (blurSigma == _blurSigma) this else Shadow(color, offsetX, offsetY, _blurSigma)
    }
}