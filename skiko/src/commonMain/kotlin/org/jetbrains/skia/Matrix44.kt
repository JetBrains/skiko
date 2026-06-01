package org.jetbrains.skia

import org.jetbrains.skia.ColorMatrix
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.withResult
import kotlin.jvm.JvmInline

/**
 *
 * 4x4 matrix used by SkCanvas and other parts of Skia.
 *
 * Skia assumes a right-handed coordinate system:
 * +X goes to the right
 * +Y goes down
 * +Z goes into the screen (away from the viewer)ç
 *
 * @param mat 16 elements in row-major order
 */
@JvmInline
value class Matrix44 internal constructor(val mat: FloatArray) {
    constructor(
        m00: Float, m01: Float, m02: Float, m03: Float,
        m10: Float, m11: Float, m12: Float, m13: Float,
        m20: Float, m21: Float, m22: Float, m23: Float,
        m30: Float, m31: Float, m32: Float, m33: Float
    ) : this(
        floatArrayOf(
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23,
            m30, m31, m32, m33,
        )
    )
    /**
     * The constructor parameters are in row-major order.
     */
    init {
        require(mat.size == 16) { "Expected 16 elements, got ${mat.size}" }
    }
    /**
     *
     * When converting from Matrix44 to Matrix33, the third row and
     * column is dropped.
     *
     * <pre>`
     * [ a b _ c ]      [ a b c ]
     * [ d e _ f ]  ->  [ d e f ]
     * [ _ _ _ _ ]      [ g h i ]
     * [ g h _ i ]
    `</pre> *
     */
    fun asMatrix33(): Matrix33 {
        return Matrix33(mat[0], mat[1], mat[3], mat[4], mat[5], mat[7], mat[12], mat[13], mat[15])
    }

    override fun toString(): String {
        return "Matrix44(mat=${mat.contentToString()})"
    }

    companion object {
        val IDENTITY = Matrix44(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)

        internal fun fromInteropPointer(block: InteropScope.(InteropPointer) -> Unit): Matrix44 {
            val result = withResult(FloatArray(16), block)
            return Matrix44(result)
        }
    }
}
