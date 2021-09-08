package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl._actualPack

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

    override fun _pack(): NativePointer = _actualPack()

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is CubicResampler) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (b.compareTo(other.b) != 0) return false
        return c.compareTo(other.c) == 0
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is CubicResampler
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