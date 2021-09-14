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

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is GradientStyle) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (isPremul != other.isPremul) return false
        val `this$_tileMode`: Any = tileMode
        val `other$_tileMode`: Any = other.tileMode
        if (`this$_tileMode` != `other$_tileMode`) return false
        val `this$_localMatrix`: Any? = localMatrix
        val `other$_localMatrix`: Any? = other.localMatrix
        return !if (`this$_localMatrix` == null) `other$_localMatrix` != null else `this$_localMatrix` != `other$_localMatrix`
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is GradientStyle
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + if (isPremul) 79 else 97
        val `$_tileMode`: Any = tileMode
        result = result * PRIME + (`$_tileMode`?.hashCode() ?: 43)
        val `$_localMatrix`: Any? = localMatrix
        result = result * PRIME + (`$_localMatrix`?.hashCode() ?: 43)
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