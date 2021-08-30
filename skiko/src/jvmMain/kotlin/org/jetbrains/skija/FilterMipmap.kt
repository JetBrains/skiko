package org.jetbrains.skija

import org.jetbrains.annotations.ApiStatus

class FilterMipmap @JvmOverloads constructor(
    internal val filterMode: FilterMode,
    internal val mipmapMode: MipmapMode = MipmapMode.NONE
) : SamplingMode {

    @ApiStatus.Internal
    override fun _pack(): Long {
        return 9223372036854775807L and (filterMode.ordinal.toLong() shl 32 or mipmapMode.ordinal.toLong())
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is FilterMipmap) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        val `this$_filterMode`: Any = filterMode
        val `other$_filterMode`: Any = other.filterMode
        if (if (`this$_filterMode` == null) `other$_filterMode` != null else `this$_filterMode` != `other$_filterMode`) return false
        val `this$_mipmapMode`: Any = mipmapMode
        val `other$_mipmapMode`: Any = other.mipmapMode
        return if (if (`this$_mipmapMode` == null) `other$_mipmapMode` != null else `this$_mipmapMode` != `other$_mipmapMode`) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is FilterMipmap
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$_filterMode`: Any = filterMode
        result = result * PRIME + (`$_filterMode`?.hashCode() ?: 43)
        val `$_mipmapMode`: Any = mipmapMode
        result = result * PRIME + (`$_mipmapMode`?.hashCode() ?: 43)
        return result
    }

    override fun toString(): String {
        return "FilterMipmap(_filterMode=" + filterMode + ", _mipmapMode=" + mipmapMode + ")"
    }
}