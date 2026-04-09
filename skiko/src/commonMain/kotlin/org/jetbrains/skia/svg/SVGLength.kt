package org.jetbrains.skia.svg

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.withResult

class SVGLength(val value: Float, val unit: SVGLengthUnit) {
    companion object {
        internal fun fromInterop(block: InteropScope.(InteropPointer) -> Unit)
            = withResult(IntArray(2), block).let {
                SVGLength(Float.fromBits(it[0]), SVGLengthUnit(it[1]))
            }
    }

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
        return if (this.unit.ordinal == _unit.ordinal) this else SVGLength(value, _unit)
    }
}