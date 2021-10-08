package org.jetbrains.skia

class FontFeature(val _tag: Int, val value: Int, val start: Long, val end: Long) {

    constructor(feature: String, value: Int, start: Long, end: Long) : this(
        FourByteTag.fromString(feature),
        value,
        start,
        end
    )

    constructor(feature: String, value: Int) : this(
        FourByteTag.fromString(feature),
        value,
        GLOBAL_START,
        GLOBAL_END
    )

    constructor(feature: String, value: Boolean) : this(
        FourByteTag.fromString(feature),
        if (value) 1 else 0,
        GLOBAL_START,
        GLOBAL_END
    )

    constructor(feature: String) : this(FourByteTag.fromString(feature), 1, GLOBAL_START, GLOBAL_END)

    val tag: String
        get() = FourByteTag.toString(_tag)

    override fun toString(): String {
        var range = ""
        if (start > 0 || end < Long.MAX_VALUE) {
            range = "[" + (if (start > 0) start else "") + ":" + (if (end < Long.MAX_VALUE) end else "") + "]"
        }
        var valuePrefix = ""
        var valueSuffix = ""
        if (value == 0) valuePrefix = "-" else if (value == 1) valuePrefix = "+" else valueSuffix = "=" + value
        return "FontFeature($valuePrefix$_tag$range$valueSuffix)"
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is FontFeature) return false
        if (this.tag != other) return false
        if (value != other.value) return false
        if (start != other.start) return false
        return end == other.end
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + (_tag.hashCode())
        result = result * PRIME + value
        result = result * PRIME + (start ushr 32 xor start).toInt()
        result = result * PRIME + (end ushr 32 xor end).toInt()
        return result
    }

    companion object {
        const val GLOBAL_START: Long = 0
        const val GLOBAL_END = Long.MAX_VALUE
        val EMPTY = arrayOfNulls<FontFeature>(0)
        val _splitPattern = compilePattern("\\s+")
        val _featurePattern =
            compilePattern("(?<sign>[-+])?(?<tag>[a-z0-9]{4})(?:\\[(?<start>\\d+)?:(?<end>\\d+)?\\])?(?:=(?<value>\\d+))?")

        fun parseOne(s: String): FontFeature {
            val m = _featurePattern.matcher(s)
            require(m.matches()) { "Can’t parse FontFeature: $s" }
            val value = if (m.group("value") != null) m.group("value")!!
                .toInt() else if (m.group("sign") == null) 1 else if ("-" == m.group("sign")) 0 else 1
            val start = if (m.group("start") == null) 0 else m.group("start")!!.toLong()
            val end = if (m.group("end") == null) Long.MAX_VALUE else m.group("end")!!.toLong()
            return FontFeature(m.group("tag")!!, value, start, end)
        }

        fun parse(str: String): Array<FontFeature?> {
            return _splitPattern.split(str)?.map { s -> parseOne(s!!) }?.toTypedArray() ?: emptyArray()
        }
    }
}