package org.jetbrains.skija

class FilterMipmap constructor(
    internal val filterMode: FilterMode,
    internal val mipmapMode: MipmapMode = MipmapMode.NONE
) : SamplingMode {

    override fun _pack(): Long {
        return filterMode.ordinal.toLong() shl 32 or mipmapMode.ordinal.toLong()
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is FilterMipmap) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        val `this$_filterMode`: Any = filterMode
        val `other$_filterMode`: Any = other.filterMode
        if (`this$_filterMode` != `other$_filterMode`) return false
        val `this$_mipmapMode`: Any = mipmapMode
        val `other$_mipmapMode`: Any = other.mipmapMode
        return `this$_mipmapMode` == `other$_mipmapMode`
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is FilterMipmap
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        val `$_filterMode`: Any = filterMode
        result = result * PRIME + (`$_filterMode`.hashCode())
        val `$_mipmapMode`: Any = mipmapMode
        result = result * PRIME + (`$_mipmapMode`.hashCode())
        return result
    }

    override fun toString(): String {
        return "FilterMipmap(_filterMode=$filterMode, _mipmapMode=$mipmapMode)"
    }
}