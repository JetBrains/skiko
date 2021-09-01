package org.jetbrains.skija.svg

class SVGLength(internal val value: Float, unit: SVGLengthUnit) {

    internal val _unit: SVGLengthUnit

    internal constructor(value: Float, unit: Int) : this(value, SVGLengthUnit.values()[unit])

    constructor(value: Float) : this(value, SVGLengthUnit.NUMBER) {}

    val unit: SVGLengthUnit
        get() = _unit

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is SVGLength) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (value.compareTo(other.value) != 0) return false
        val `this$_unit`: Any = unit
        val `other$_unit`: Any = other.unit
        return `this$_unit` == `other$_unit`
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is SVGLength
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + java.lang.Float.floatToIntBits(value)
        val `$_unit`: Any = unit
        result = result * PRIME + (`$_unit`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "SVGLength(_value=$value, _unit=$unit)"
    }

    fun withValue(_value: Float): SVGLength {
        return if (value == _value) this else SVGLength(_value, _unit)
    }

    fun withUnit(_unit: SVGLengthUnit): SVGLength {
        return if (this._unit === _unit) this else SVGLength(value, _unit)
    }

    init {
        _unit = unit
    }
}