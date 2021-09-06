package org.jetbrains.skia.svg

import org.jetbrains.skia.*
import kotlin.math.hypot
import kotlin.math.sqrt

class SVGLengthContext constructor(
    internal val width: Float,
    internal val height: Float,
    internal val dpi: Float = 90f
) {

    constructor(size: Point) : this(size.x, size.y, 90f)

    fun resolve(length: SVGLength, type: SVGLengthType): Float {
        return when (length._unit) {
            SVGLengthUnit.NUMBER -> length.value
            SVGLengthUnit.PX -> length.value
            SVGLengthUnit.PERCENTAGE -> {
                when (type) {
                    SVGLengthType.HORIZONTAL -> return length.value * width / 100.0f
                    SVGLengthType.VERTICAL -> return length.value * height / 100.0f
                    SVGLengthType.OTHER ->                 // https://www.w3.org/TR/SVG11/coords.html#Units_viewport_percentage
                        return (length.value * hypot(
                            width.toDouble(),
                            height.toDouble()
                        ) / sqrt(2.0) / 100.0).toFloat()
                }
            }
            SVGLengthUnit.CM -> length.value * dpi / 2.54f
            SVGLengthUnit.MM -> length.value * dpi / 25.4f
            SVGLengthUnit.IN -> length.value * dpi
            SVGLengthUnit.PT -> length.value * dpi / 72.272f
            SVGLengthUnit.PC -> length.value * dpi * 12.0f / 72.272f
            else -> throw IllegalArgumentException("Unknown SVGLengthUnit: " + length._unit)
        }
    }

    fun resolveRect(x: SVGLength, y: SVGLength, width: SVGLength, height: SVGLength): Rect {
        return Rect.Companion.makeXYWH(
            resolve(x, SVGLengthType.HORIZONTAL),
            resolve(y, SVGLengthType.VERTICAL),
            resolve(width, SVGLengthType.HORIZONTAL),
            resolve(height, SVGLengthType.VERTICAL)
        )
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is SVGLengthContext) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (width.compareTo(other.width) != 0) return false
        if (height.compareTo(other.height) != 0) return false
        return dpi.compareTo(other.dpi) == 0
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is SVGLengthContext
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + width.toBits()
        result = result * PRIME + height.toBits()
        result = result * PRIME + dpi.toBits()
        return result
    }

    override fun toString(): String {
        return "SVGLengthContext(_width=$width, _height=$height, _dpi=$dpi)"
    }

    fun withWidth(_width: Float): SVGLengthContext {
        return if (width == _width) this else SVGLengthContext(_width, height, dpi)
    }

    fun withHeight(_height: Float): SVGLengthContext {
        return if (height == _height) this else SVGLengthContext(width, _height, dpi)
    }

    fun withDpi(_dpi: Float): SVGLengthContext {
        return if (dpi == _dpi) this else SVGLengthContext(width, height, _dpi)
    }
}