package org.jetbrains.skia

/**
 * @see org.jetbrains.skia.FilterMipmap
 *
 * @see org.jetbrains.skia.CubicResampler
 */
interface SamplingMode {
    @Deprecated("Long can't be used because Long is an object in kotlin/js. Consider using _packedInt1 and _packedInt2")
    fun _pack(): Long

    // _packedInt1 and _packedInt2 are used to serialize SamplingMode instances for interop
    fun _packedInt1(): Int
    fun _packedInt2(): Int

    companion object {
        val DEFAULT: SamplingMode = FilterMipmap(FilterMode.NEAREST, MipmapMode.NONE)
        val LINEAR: SamplingMode = FilterMipmap(FilterMode.LINEAR, MipmapMode.NONE)
        val MITCHELL: SamplingMode = CubicResampler(0.33333334f, 0.33333334f)
        val CATMULL_ROM: SamplingMode = CubicResampler(0f, 0.5f)
    }
}
