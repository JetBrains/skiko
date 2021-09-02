package org.jetbrains.skia

class ColorMatrix(vararg mat: Float) {
    val mat: FloatArray
    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is ColorMatrix) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        return mat.contentEquals(other.mat)
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is ColorMatrix
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + mat.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "ColorMatrix(_mat=$mat)"
    }

    init {
        require(mat.size == 20) { "Expected 20 elements, got $mat" }
        this.mat = mat
    }
}