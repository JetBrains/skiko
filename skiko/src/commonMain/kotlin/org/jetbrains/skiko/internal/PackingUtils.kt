package org.jetbrains.skiko.internal

// These two functions are technically identical to Float.fromBits()
// and Double.fromBits(). However, since they are declared as top-
// level functions, they do not incur the cost of a static fetch
// through the Companion class. Using these top-level functions,
// the generated arm64 code after dex2oat is exactly a single `fmov`
/** Returns the [Float] value corresponding to a given bit representation. */
expect fun floatFromBits(bits: Int): Float

// Same as Offset/Size.Unspecified.packedValue, but avoids a getstatic
@PublishedApi internal const val UnspecifiedPackedFloats: Long = 0x7fc00000_7fc00000L // NaN_NaN
/** Packs two Int values into one Long value for use in inline classes. */
public inline fun packInts(val1: Int, val2: Int): Long {
    return (val1.toLong() shl 32) or (val2.toLong() and 0xFFFFFFFF)
}

/** Unpacks the first Int value in [packInts] from its returned ULong. */
public inline fun unpackInt1(value: Long): Int {
    return (value shr 32).toInt()
}

/** Unpacks the second Int value in [packInts] from its returned ULong. */
public inline fun unpackInt2(value: Long): Int {
    return (value and 0xFFFFFFFF).toInt()
}

/** Packs two Float values into one Long value for use in inline classes. */
public inline fun packFloats(val1: Float, val2: Float): Long {
    val v1 = val1.toRawBits().toLong()
    val v2 = val2.toRawBits().toLong()
    return (v1 shl 32) or (v2 and 0xFFFFFFFF)
}

/** Unpacks the first Float value in [packFloats] from its returned Long. */
public inline fun unpackFloat1(value: Long): Float {
    return floatFromBits((value shr 32).toInt())
}
/** Unpacks the second Float value in [packFloats] from its returned Long. */
public inline fun unpackFloat2(value: Long): Float {
    return floatFromBits((value and 0xFFFFFFFF).toInt())
}