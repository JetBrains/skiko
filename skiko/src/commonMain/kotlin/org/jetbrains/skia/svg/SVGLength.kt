package org.jetbrains.skia.svg

class SVGLength(val value: Float, val unit: SVGLengthUnit) {

    internal constructor(value: Float, unit: Int) : this(value, SVGLengthUnit.values()[unit])

    constructor(value: Float) : this(value, SVGLengthUnit.NUMBER) {}

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is SVGLength) return false
        if (value.compareTo(other.value) != 0) return false
        return unit == other.unit
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + value.toBits()
        result = result * PRIME + unit.hashCode()
        return result
    }

    override fun toString(): String {
        return "SVGLength(_value=$value, _unit=$unit)"
    }

    fun withValue(_value: Float): SVGLength {
        return if (value == _value) this else SVGLength(_value, unit)
    }

    fun withUnit(_unit: SVGLengthUnit): SVGLength {
        return if (this.unit === unit) this else SVGLength(value, unit)
    }
}