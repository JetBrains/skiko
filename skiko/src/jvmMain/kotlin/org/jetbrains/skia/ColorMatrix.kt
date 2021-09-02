package org.jetbrains.skia

import java.util.*

class ColorMatrix(vararg mat: Float) {
    val mat: FloatArray
    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is ColorMatrix) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        return if (!Arrays.equals(mat, other.mat)) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is ColorMatrix
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + Arrays.hashCode(mat)
        return result
    }

    override fun toString(): String {
        return "ColorMatrix(_mat=" + Arrays.toString(mat) + ")"
    }

    init {
        assert(mat.size == 20) { (if ("Expected 20 elements, got $mat" == null) null else mat.size)!! }
        this.mat = mat
    }
}