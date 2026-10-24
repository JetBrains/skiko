package org.jetbrains.skia

inline fun FilterMipmap(filterMode: FilterMode, mipmapMode: MipmapMode = MipmapMode.NONE): SamplingMode =
    SamplingMode(filterMode.ordinal.toLong() shl 32 or mipmapMode.ordinal.toLong())

val SamplingMode.isFilterMipmap: Boolean
    get() = (packedValue and Long.MIN_VALUE) == 0L

/**
 * @throws IllegalArgumentException If this SamplingMode is not a filter mipmap
 * @see [SamplingMode.isFilterMipmap]
 */
val SamplingMode.filterMode: FilterMode
    get() {
        require(isFilterMipmap)
        return FilterMode.entries[(packedValue shr 32).toInt()]
    }

/**
 * @throws IllegalArgumentException If this SamplingMode is not a filter mipmap
 * @see [SamplingMode.isFilterMipmap]
 */
val SamplingMode.mipmapMode: MipmapMode
    get() {
        require(isFilterMipmap)
        return MipmapMode.entries[(packedValue and 0xFFFFFFFFL).toInt()]
    }
