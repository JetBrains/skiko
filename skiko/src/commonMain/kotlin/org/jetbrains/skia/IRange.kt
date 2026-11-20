package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.withResult
import org.jetbrains.skiko.internal.packInts
import org.jetbrains.skiko.internal.unpackInt1
import org.jetbrains.skiko.internal.unpackInt2
import kotlin.jvm.JvmInline

inline fun IRange(start: Int, end: Int): IRange = IRange(packInts(start, end))
@JvmInline
value class IRange(val packedValue: Long) {

    inline val start: Int
        get() = unpackInt1(packedValue)

    inline val end: Int
        get() = unpackInt2(packedValue)

    override fun toString(): String {
        return "IRange(start=$start, end=$end)"
    }

    companion object {}
}

internal fun IRange.Companion.fromInteropPointer(block: InteropScope.(InteropPointer) -> Unit): IRange {
    val result = withResult(IntArray(2), block)
    return IRange(result[0], result[1])
}