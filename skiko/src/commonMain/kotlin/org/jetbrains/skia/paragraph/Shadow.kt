package org.jetbrains.skia.paragraph

import org.jetbrains.skia.Point

class Shadow(val color: Int, val offsetX: Float, val offsetY: Float, val blurSigma: Double) {

    constructor(color: Int, offset: Point, blurSigma: Double) : this(color, offset.x, offset.y, blurSigma)

    val offset: Point
        get() = Point(offsetX, offsetY)

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Shadow) return false
        if (color != other.color) return false
        if (offsetX.compareTo(other.offsetX) != 0) return false
        if (offsetY.compareTo(other.offsetY) != 0) return false
        return blurSigma.compareTo(other.blurSigma) == 0
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + color
        result = result * PRIME + offsetX.toBits()
        result = result * PRIME + offsetY.toBits()
        val blurSigma = blurSigma.toBits()
        result = result * PRIME + (blurSigma ushr 32 xor blurSigma).toInt()
        return result
    }

    override fun toString(): String {
        return "Shadow(_color=$color, _offsetX=$offsetX, _offsetY=$offsetY, _blurSigma=$blurSigma)"
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