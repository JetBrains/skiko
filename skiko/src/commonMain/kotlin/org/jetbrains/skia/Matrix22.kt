package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.withResult

/**
 * 2x2 matrix.
 */
class Matrix22(vararg mat: Float) {
    /**
     * Matrix elements are in row-major order.
     */
    internal val mat: FloatArray

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Matrix22) return false
        return mat.contentEquals(other.mat)
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + mat.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "Matrix22(_mat=$mat)"
    }

    companion object {
        internal fun fromInteropPointer(block: InteropScope.(InteropPointer) -> Unit): Matrix22 {
            val result = withResult(FloatArray(4), block)
            return Matrix22(*result)
        }
    }

    /**
     * The constructor parameters are in row-major order.
     */
    init {
        require(mat.size == 4) { "Expected 4 elements, got ${mat.size}" }
        this.mat = mat
    }
}
