package org.jetbrains.skija

import java.util.regex.Pattern

class FontFeature(val _tag: Int, val value: Int, val start: Long, val end: Long) {

    constructor(feature: String, value: Int, start: Long, end: Long) : this(
        FourByteTag.Companion.fromString(feature),
        value,
        start,
        end
    )

    constructor(feature: String, value: Int) : this(
        FourByteTag.Companion.fromString(feature),
        value,
        GLOBAL_START,
        GLOBAL_END
    )

    constructor(feature: String, value: Boolean) : this(
        FourByteTag.Companion.fromString(feature),
        if (value) 1 else 0,
        GLOBAL_START,
        GLOBAL_END
    )

    constructor(feature: String) : this(FourByteTag.Companion.fromString(feature), 1, GLOBAL_START, GLOBAL_END)

    val tag: String
        get() = FourByteTag.Companion.toString(_tag)

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

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is FontFeature) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        val `this$_tag`: Any = _tag
        val `other$_tag`: Any = other._tag
        if (if (`this$_tag` == null) `other$_tag` != null else `this$_tag` != `other$_tag`) return false
        if (value != other.value) return false
        if (start != other.start) return false
        return if (end != other.end) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is FontFeature
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$_tag`: Any = _tag
        result = result * PRIME + (`$_tag`?.hashCode() ?: 43)
        result = result * PRIME + value
        val `$_start` = start
        result = result * PRIME + (`$_start` ushr 32 xor `$_start`).toInt()
        val `$_end` = end
        result = result * PRIME + (`$_end` ushr 32 xor `$_end`).toInt()
        return result
    }

    companion object {
        const val GLOBAL_START: Long = 0
        const val GLOBAL_END = Long.MAX_VALUE
        val EMPTY = arrayOfNulls<FontFeature>(0)
        val _splitPattern = Pattern.compile("\\s+")
        val _featurePattern =
            Pattern.compile("(?<sign>[-+])?(?<tag>[a-z0-9]{4})(?:\\[(?<start>\\d+)?:(?<end>\\d+)?\\])?(?:=(?<value>\\d+))?")

        fun parseOne(s: String): FontFeature {
            val m = _featurePattern.matcher(s)
            require(m.matches()) { "Can’t parse FontFeature: $s" }
            val value = if (m.group("value") != null) m.group("value")
                .toInt() else if (m.group("sign") == null) 1 else if ("-" == m.group("sign")) 0 else 1
            val start = if (m.group("start") == null) 0 else m.group("start").toLong()
            val end = if (m.group("end") == null) Long.MAX_VALUE else m.group("end").toLong()
            return FontFeature(m.group("tag"), value, start, end)
        }

        fun parse(s: String?): Array<FontFeature?> {
            return _splitPattern.split(s).map { s -> parseOne(s) }.toTypedArray()
        }
    }
}