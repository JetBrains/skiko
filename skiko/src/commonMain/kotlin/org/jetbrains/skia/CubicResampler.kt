package org.jetbrains.skia

/**
 *
 * Specify B and C (each between 0...1) to create a shader that applies the corresponding
 * cubic reconstruction filter to the image.
 *
 *
 * Example values:
 * <dl>
 * <dt>B = 1/3, C = 1/3</dt><dd>"Mitchell" filter</dd>
 * <dt>B = 0,   C = 1/2</dt><dd>"Catmull-Rom" filter</dd>
</dl> *
 *
 *
 * See
 *
 *  * "Reconstruction Filters in Computer Graphics" Don P. Mitchell, Arun N. Netravali, 1988
 * [https://www.cs.utexas.edu/~fussell/courses/cs384g-fall2013/lectures/mitchell/Mitchell.pdf](https://www.cs.utexas.edu/~fussell/courses/cs384g-fall2013/lectures/mitchell/Mitchell.pdf)
 *  * Desmos worksheet [https://www.desmos.com/calculator/aghdpicrvr](https://www.desmos.com/calculator/aghdpicrvr)
 *  * Nice overview [https://entropymine.com/imageworsener/bicubic/](https://entropymine.com/imageworsener/bicubic/)
 *
 */
inline fun CubicResampler(b: Float, c: Float): SamplingMode =
    SamplingMode((0x8L shl 60) or ((b.toBits().toULong() shl 32) or c.toBits().toULong()).toLong())

val SamplingMode.isCubicResampler: Boolean
    get() = (packedValue and Long.MIN_VALUE) != 0L

/**
 * @throws IllegalArgumentException If this SamplingMode is not a cubic resampler
 * @see [SamplingMode.isCubicResampler]
 */
val SamplingMode.b: Float
    get() {
        require(isCubicResampler)
        return Float.fromBits(((packedValue and Long.MAX_VALUE) ushr 32).toInt())
    }

/**
 * @throws IllegalArgumentException If this SamplingMode is not a cubic resampler
 * @see [SamplingMode.isCubicResampler]
 */
val SamplingMode.c: Float
    get() {
        require(isCubicResampler)
        return Float.fromBits((packedValue and 0xFFFFFFFFL).toInt())
    }