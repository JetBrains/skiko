package org.jetbrains.skia

/**
 *
 * 4x4 matrix used by SkCanvas and other parts of Skia.
 *
 * Skia assumes a right-handed coordinate system:
 * +X goes to the right
 * +Y goes down
 * +Z goes into the screen (away from the viewer)
 */
class Matrix44(vararg mat: Float) {
    /**
     * Matrix elements are in row-major order.
     */
    /**
     * Matrix elements are in row-major order.
     */
    internal val mat: FloatArray

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

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is Matrix44) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        return mat.contentEquals(other.mat)
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is Matrix44
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + mat.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "Matrix44(_mat=$mat)"
    }

    companion object {
        val IDENTITY = Matrix44(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
    }

    /**
     * The constructor parameters are in row-major order.
     */
    init {
        require(mat.size == 16) { "Expected 16 elements, got ${mat.size}" }
        this.mat = mat
    }
}