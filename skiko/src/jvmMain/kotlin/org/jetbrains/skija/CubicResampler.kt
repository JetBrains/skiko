package org.jetbrains.skija

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
class CubicResampler(internal val b: Float, internal val c: Float) : SamplingMode {

    override fun _pack(): Long {
        return ((java.lang.Float.floatToIntBits(b).toULong() shl 32) or
                java.lang.Float.floatToIntBits(c).toULong()).toLong()
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is CubicResampler) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (java.lang.Float.compare(b, other.b) != 0) return false
        return if (java.lang.Float.compare(c, other.c) != 0) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is CubicResampler
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + java.lang.Float.floatToIntBits(b)
        result = result * PRIME + java.lang.Float.floatToIntBits(c)
        return result
    }

    override fun toString(): String {
        return "CubicResampler(_B=" + b + ", _C=" + c + ")"
    }
}