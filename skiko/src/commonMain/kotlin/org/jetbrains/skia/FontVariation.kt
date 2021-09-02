package org.jetbrains.skia

class FontVariation(val _tag: Int, val value: Float) {

    constructor(feature: String, value: Float) : this(FourByteTag.fromString(feature), value) {}

    val tag: String
        get() = FourByteTag.toString(_tag)

    override fun toString(): String {
        return "$tag=$value"
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is FontVariation) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        val `this$_tag`: Any = _tag
        val `other$_tag`: Any = other._tag
        if (`this$_tag` != `other$_tag`) return false
        return value.compareTo(other.value) == 0
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is FontVariation
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$_tag`: Any = _tag
        result = result * PRIME + (`$_tag`?.hashCode() ?: 43)
        result = result * PRIME + value.toBits()
        return result
    }

    companion object {
        val EMPTY = arrayOfNulls<FontVariation>(0)

        internal val _splitPattern = compilePattern("\\s+")

        internal val _variationPattern = compilePattern("(?<tag>[a-z0-9]{4})=(?<value>\\d+)")

        fun parseOne(s: String): FontVariation {
            val m = _variationPattern.matcher(s)
            require(m.matches()) { "Can’t parse FontVariation: $s" }
            val value = m.group("value")!!.toFloat()
            return FontVariation(m.group("tag")!!, value)
        }

        fun parse(s: String?): Array<FontVariation> {
            return _splitPattern.split(s!!)!!.map { s -> parseOne(s!!) }
                .toTypedArray()
        }
    }
}