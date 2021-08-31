package org.jetbrains.skija

class FontVariationAxis(
    internal val _tag: Int,
    internal val minValue: Float,
    internal val defaultValue: Float,
    internal val maxValue: Float,
    internal val isHidden: Boolean
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
        FourByteTag.Companion.fromString(tag),
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
        if (if (`this$_tag` == null) `other$_tag` != null else `this$_tag` != `other$_tag`) return false
        if (java.lang.Float.compare(minValue, other.minValue) != 0) return false
        if (java.lang.Float.compare(defaultValue, other.defaultValue) != 0) return false
        if (java.lang.Float.compare(maxValue, other.maxValue) != 0) return false
        return if (isHidden != other.isHidden) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is FontVariationAxis
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$_tag`: Any = _tag
        result = result * PRIME + (`$_tag`?.hashCode() ?: 43)
        result = result * PRIME + java.lang.Float.floatToIntBits(minValue)
        result = result * PRIME + java.lang.Float.floatToIntBits(defaultValue)
        result = result * PRIME + java.lang.Float.floatToIntBits(maxValue)
        result = result * PRIME + if (isHidden) 79 else 97
        return result
    }

    override fun toString(): String {
        return "FontVariationAxis(_tag=" + _tag + ", _minValue=" + minValue + ", _defaultValue=" + defaultValue + ", _maxValue=" + maxValue + ", _hidden=" + isHidden + ")"
    }
}