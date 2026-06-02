package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.withResult
import kotlin.jvm.JvmInline

/**
 * 2x2 matrix.
 * @param mat 4 elements in row-major order
 */
@JvmInline
value class Matrix22 internal constructor(val mat: FloatArray) {
    constructor(
        m00: Float, m01: Float,
        m10: Float, m11: Float
    ) : this(
        floatArrayOf(
            m00, m01,
            m10, m11
        )
    )

    /**
     * The constructor parameters are in row-major order.
     */
    init {
        require(mat.size == 4) { "Expected 4 elements, got ${mat.size}" }
    }
    override fun toString(): String {
        return "Matrix22(mat=${mat.contentToString()})"
    }

    companion object {
        val IDENTITY = Matrix22(1f, 0f, 0f, 1f)

        internal fun fromInteropPointer(block: InteropScope.(InteropPointer) -> Unit): Matrix22 {
            val result = withResult(FloatArray(4), block)
            return Matrix22(result)
        }
    }
}
