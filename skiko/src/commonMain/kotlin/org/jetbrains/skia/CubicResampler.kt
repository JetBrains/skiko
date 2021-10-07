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
class CubicResampler(val b: Float, val c: Float) : SamplingMode {

    override fun _pack(): Long = (0x8L shl 60) or ((b.toBits().toULong() shl 32) or c.toBits().toULong()).toLong()

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is CubicResampler) return false
        if (b.compareTo(other.b) != 0) return false
        return c.compareTo(other.c) == 0
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + b.toBits()
        result = result * PRIME + c.toBits()
        return result
    }

    override fun toString(): String {
        return "CubicResampler(_B=$b, _C=$c)"
    }
}
