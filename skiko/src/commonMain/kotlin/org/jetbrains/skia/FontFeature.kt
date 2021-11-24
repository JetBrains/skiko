package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.InteropScope

class FontFeature(val _tag: Int, val value: Int, val start: UInt, val end: UInt) {

    constructor(feature: String, value: Int, start: UInt, end: UInt) : this(
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
        if (start > 0u || end < UInt.MAX_VALUE) {
            range = "[" + (if (start > 0u) start else "") + ":" + (if (end < UInt.MAX_VALUE) end else "") + "]"
        }
        var valuePrefix = ""
        var valueSuffix = ""
        if (value == 0) valuePrefix = "-" else if (value == 1) valuePrefix = "+" else valueSuffix = "=" + value
        return "FontFeature($valuePrefix$_tag$range$valueSuffix)"
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is FontFeature) return false
        if (this.tag != other.tag) return false
        if (value != other.value) return false
        if (start != other.start) return false
        return end == other.end
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + (_tag.hashCode())
        result = result * PRIME + value
        result = result * PRIME + (start shr 16 xor start).toInt()
        result = result * PRIME + (end shr 16 xor end).toInt()
        return result
    }

    internal fun toInteropIntArray(): IntArray {
        return intArrayOf(_tag, value, start.toInt(), end.toInt())
    }

    internal fun InteropScope.toInterop(): InteropPointer {
        return toInterop(toInteropIntArray())
    }

    companion object {
        const val GLOBAL_START: UInt = 0u
        // according to https://github.com/google/skia/blob/main/modules/skshaper/src/SkShaper_harfbuzz.cpp#L48
        // define HB_FEATURE_GLOBAL_END ((unsigned int) -1)
        const val GLOBAL_END: UInt = UInt.MAX_VALUE

        val EMPTY = arrayOfNulls<FontFeature>(0)
        val _splitPattern = compilePattern("\\s+")
        val _featurePattern =
            compilePattern("([-+])?([a-z0-9]{4})(?:\\[(\\d+)?:(\\d+)?\\])?(?:=(\\d+))?")

        // We can't use named groups (not supported in k/n), so we use numeric groups.
        // These constants are group indexes of _featurePattern:
        private const val signIx = 1
        private const val tagIx = 2
        private const val startIx = 3
        private const val endIx = 4
        private const val valueIx = 5

        fun parseOne(s: String): FontFeature {
            val m = _featurePattern.matcher(s)
            require(m.matches()) { "Can’t parse FontFeature: $s" }
            val value = if (m.group(valueIx) != null) m.group(valueIx)!!
                .toInt() else if (m.group(signIx) == null) 1 else if ("-" == m.group(signIx)) 0 else 1
            val start = if (m.group(startIx) == null) 0u else m.group(startIx)!!.toUInt()
            val end = if (m.group(endIx) == null) GLOBAL_END else m.group(endIx)!!.toUInt()
            return FontFeature(m.group(tagIx)!!, value, start, end)
        }

        fun parse(str: String): Array<FontFeature> {
            return _splitPattern.split(str).map { s -> parseOne(s) }.toTypedArray()
        }

        internal fun InteropScope.arrayOfFontFeaturesToInterop(fontFeatures: Array<FontFeature>?): InteropPointer {
            val ints = IntArray(4 * (fontFeatures?.size ?: 0))

            fontFeatures?.forEachIndexed { ix, fontFeature ->
                val j = ix * 4
                ints[j] = fontFeature._tag
                ints[j + 1] = fontFeature.value
                ints[j + 2] = fontFeature.start.toInt()
                ints[j + 3] = fontFeature.end.toInt()
            }

            return toInterop(ints)
        }

        /**
         * This function can be used to convert IntArray to FontFeatures.
         * Every FontFeature is represented by 2 ints (_tag, value)
         */
        internal fun fromInteropEncodedBy2Ints(fontFeatures: IntArray): Array<FontFeature> {
            val featuresCount = fontFeatures.size / 2

            return Array(featuresCount) {
                val j = it * 2
                FontFeature(
                    fontFeatures[j], fontFeatures[j + 1],
                    GLOBAL_START, GLOBAL_END
                )
            }
        }
    }
}
