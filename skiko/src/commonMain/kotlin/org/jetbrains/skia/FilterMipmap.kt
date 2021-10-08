package org.jetbrains.skia

class FilterMipmap constructor(
    internal val filterMode: FilterMode,
    internal val mipmapMode: MipmapMode = MipmapMode.NONE
) : SamplingMode {

    override fun _pack() = filterMode.ordinal.toLong() shl 32 or mipmapMode.ordinal.toLong()

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is FilterMipmap) return false
        if (this.filterMode != other.filterMode) return false
        return this.mipmapMode == other.mipmapMode
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + (filterMode.hashCode())
        result = result * PRIME + (mipmapMode.hashCode())
        return result
    }

    override fun toString(): String {
        return "FilterMipmap(_filterMode=$filterMode, _mipmapMode=$mipmapMode)"
    }
}