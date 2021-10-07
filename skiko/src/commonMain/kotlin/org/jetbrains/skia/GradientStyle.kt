package org.jetbrains.skia

class GradientStyle(
    val tileMode: FilterTileMode,
    val isPremul: Boolean,
    val localMatrix: Matrix33?
) {

    internal fun _getFlags(): Int {
        return 0 or if (isPremul) _INTERPOLATE_PREMUL else 0
    }

    internal fun _getMatrixArray(): FloatArray? {
        return localMatrix?.mat
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is GradientStyle) return false
        if (isPremul != other.isPremul) return false
        if (this.tileMode != other.tileMode) return false
        return !if (this.localMatrix == null) other.localMatrix != null else this.localMatrix != other.localMatrix
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + if (isPremul) 79 else 97
        result = result * PRIME + tileMode.hashCode()
        result = result * PRIME + (localMatrix?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "GradientStyle(_tileMode=$tileMode, _premul=$isPremul, _localMatrix=$localMatrix)"
    }

    fun withTileMode(_tileMode: FilterTileMode): GradientStyle {
        return if (tileMode == _tileMode) this else GradientStyle(_tileMode, isPremul, localMatrix)
    }

    fun withPremul(_premul: Boolean): GradientStyle {
        return if (isPremul == _premul) this else GradientStyle(tileMode, _premul, localMatrix)
    }

    fun withLocalMatrix(_localMatrix: Matrix33): GradientStyle {
        return if (localMatrix === _localMatrix) this else GradientStyle(tileMode, isPremul, _localMatrix)
    }

    companion object {
        internal val _INTERPOLATE_PREMUL = 1
        var DEFAULT = GradientStyle(FilterTileMode.CLAMP, true, null)
    }
}