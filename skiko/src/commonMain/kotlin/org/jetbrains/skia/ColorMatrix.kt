package org.jetbrains.skia

class ColorMatrix(vararg mat: Float) {
    val mat: FloatArray
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is ColorMatrix) return false
        return mat.contentEquals(other.mat)
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