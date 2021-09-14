package org.jetbrains.skia

class FontVariationAxis(
    val _tag: Int,
    val minValue: Float,
    val defaultValue: Float,
    val maxValue: Float,
    val isHidden: Boolean
) {

    val tag: String
        get() = FourByteTag.toString(_tag)

    constructor(
        tag: String,
        min: Float,
        def: Float,
        max: Float,
        hidden: Boolean
    ) : this(FourByteTag.fromString(tag), min, def, max, hidden) {
    }

    constructor(tag: String, min: Float, def: Float, max: Float) : this(
        FourByteTag.fromString(tag),
        min,
        def,
        max,
        false
    ) {
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is FontVariationAxis) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        val `this$_tag`: Any = _tag
        val `other$_tag`: Any = other._tag
        if (`this$_tag` != `other$_tag`) return false
        if (minValue.compareTo(other.minValue) != 0) return false
        if (defaultValue.compareTo(other.defaultValue) != 0) return false
        if (maxValue.compareTo(other.maxValue) != 0) return false
        return isHidden == other.isHidden
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is FontVariationAxis
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$_tag`: Any = _tag
        result = result * PRIME + (`$_tag`.hashCode())
        result = result * PRIME + minValue.toBits()
        result = result * PRIME + defaultValue.toBits()
        result = result * PRIME + maxValue.toBits()
        result = result * PRIME + if (isHidden) 79 else 97
        return result
    }

    override fun toString(): String {
        return "FontVariationAxis(_tag=$_tag, _minValue=$minValue, _defaultValue=$defaultValue, _maxValue=$maxValue, _hidden=$isHidden)"
    }
}