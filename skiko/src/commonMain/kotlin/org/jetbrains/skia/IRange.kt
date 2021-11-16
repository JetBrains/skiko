package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.withResult

class IRange(val start: Int, val end: Int) {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is IRange) return false
        if (start != other.start) return false
        return end == other.end
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + start
        result = result * PRIME + end
        return result
    }

    override fun toString(): String {
        return "IRange(_start=$start, _end=$end)"
    }

    companion object {}
}

internal fun IRange.Companion.fromInteropPointer(block: InteropScope.(InteropPointer) -> Unit): IRange {
    val result = withResult(IntArray(2), block)
    return IRange(result[0], result[1])
}