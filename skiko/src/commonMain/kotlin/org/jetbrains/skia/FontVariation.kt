package org.jetbrains.skia

class FontVariation(val _tag: Int, val value: Float) {

    constructor(feature: String, value: Float) : this(FourByteTag.fromString(feature), value) {}

    val tag: String
        get() = FourByteTag.toString(_tag)

    override fun toString(): String {
        return "$tag=$value"
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is FontVariation) return false
        if (this.tag != other.tag) return false
        return value.compareTo(other.value) == 0
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + _tag
        result = result * PRIME + value.toBits()
        return result
    }

    companion object {
        val EMPTY = arrayOfNulls<FontVariation>(0)

        internal val _splitPattern = compilePattern("\\s+")

        internal val _variationPattern = compilePattern("([a-z0-9]{4})=(\\d+)")

        private val groupsIx = mapOf("tag" to 1, "value" to 2)

        fun parseOne(s: String): FontVariation {
            val m = _variationPattern.matcher(s)
            require(m.matches()) { "Can’t parse FontVariation: $s" }
            val value = m.group(groupsIx["value"]!!)!!.toFloat()
            val tag = m.group(groupsIx["tag"]!!)!!
            return FontVariation(tag, value)
        }

        fun parse(str: String): Array<FontVariation> {
            return _splitPattern.split(str)!!.map { s -> parseOne(s!!) }
                .toTypedArray()
        }
    }
}
