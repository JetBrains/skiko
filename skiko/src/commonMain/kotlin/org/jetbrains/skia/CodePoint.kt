package org.jetbrains.skia

// TODO Remove once it's available in common stdlib https://youtrack.jetbrains.com/issue/KT-23251
internal typealias CodePoint = Int

/**
 * The minimum value of a supplementary code point, `\u0x10000`.
 */
private const val MIN_SUPPLEMENTARY_CODE_POINT: Int = 0x10000

/**
 * Converts a surrogate pair to a unicode code point.
 */
internal fun toCodePoint(high: Char, low: Char): CodePoint =
    (((high - Char.MIN_HIGH_SURROGATE) shl 10) or (low - Char.MIN_LOW_SURROGATE)) + MIN_SUPPLEMENTARY_CODE_POINT

internal fun Int.charCount(): Int = if (this >= MIN_SUPPLEMENTARY_CODE_POINT) 2 else 1

internal val CharSequence.codePoints
    get() = codePointsAt(0)

internal fun CharSequence.codePointsAt(index: Int) = sequence {
    var current = index
    while (current < length) {
        val codePoint = codePointAt(current)
        yield(codePoint)
        current += codePoint.charCount()
    }
}

/**
 * Decodes all code points into an [IntArray] in a single pass with no boxing or intermediate collections.
 */
internal val CharSequence.codePointsAsIntArray: IntArray
    get() {
        val len = length
        if (len == 0) return IntArray(0)
        // Upper bound: every char is a separate BMP code point.
        // If surrogate pairs exist, we shrink via copyOf.
        val buffer = IntArray(len)
        var count = 0
        var i = 0
        while (i < len) {
            val high = this[i]
            i++
            if (high.isHighSurrogate() && i < len) {
                val low = this[i]
                if (low.isLowSurrogate()) {
                    i++
                    buffer[count++] = toCodePoint(high, low)
                    continue
                }
            }
            buffer[count++] = high.code
        }
        return if (count == len) buffer else buffer.copyOf(count)
    }

/**
 * Returns the character (Unicode code point) at the specified index.
 */
internal fun CharSequence.codePointAt(index: Int): CodePoint {
    val high = this[index]
    if (high.isHighSurrogate() && index + 1 < this.length) {
        val low = this[index + 1]
        if (low.isLowSurrogate()) {
            return toCodePoint(high, low)
        }
    }
    return high.code
}
