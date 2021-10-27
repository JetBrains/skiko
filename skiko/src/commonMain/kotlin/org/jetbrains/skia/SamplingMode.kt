package org.jetbrains.skia

/**
 * @see org.jetbrains.skia.FilterMipmap
 *
 * @see org.jetbrains.skia.CubicResampler
 */
interface SamplingMode {
    fun _pack(): Long
    fun _packAs2Ints(): IntArray

    companion object {
        val DEFAULT: SamplingMode = FilterMipmap(FilterMode.NEAREST, MipmapMode.NONE)
        val LINEAR: SamplingMode = FilterMipmap(FilterMode.LINEAR, MipmapMode.NONE)
        val MITCHELL: SamplingMode = CubicResampler(0.33333334f, 0.33333334f)
        val CATMULL_ROM: SamplingMode = CubicResampler(0f, 0.5f)
    }
}
