package org.jetbrains.skija

/**
 * @see org.jetbrains.skija.FilterMipmap
 *
 * @see org.jetbrains.skija.CubicResampler
 */
interface SamplingMode {
    fun _pack(): Long

    companion object {
        val DEFAULT: SamplingMode = FilterMipmap(FilterMode.NEAREST, MipmapMode.NONE)
        val LINEAR: SamplingMode = FilterMipmap(FilterMode.LINEAR, MipmapMode.NONE)
        val MITCHELL: SamplingMode = CubicResampler(0.33333334f, 0.33333334f)
        val CATMULL_ROM: SamplingMode = CubicResampler(0f, 0.5f)
    }
}