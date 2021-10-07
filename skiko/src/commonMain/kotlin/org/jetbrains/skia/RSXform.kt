package org.jetbrains.skia

import kotlin.math.cos
import kotlin.math.sin

/**
 *
 * A compressed form of a rotation+scale matrix.
 *
 * <pre>[ fSCos     -fSSin    fTx ]
 * [ fSSin      fSCos    fTy ]
 * [     0          0      1 ]</pre>
 */
class RSXform(
    internal val scos: Float,
    internal val ssin: Float,
    internal val tx: Float,
    internal val ty: Float
) {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is RSXform) return false
        if (scos.compareTo(other.scos) != 0) return false
        if (ssin.compareTo(other.ssin) != 0) return false
        if (tx.compareTo(other.tx) != 0) return false
        return ty.compareTo(other.ty) == 0
    }


    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + scos.toBits()
        result = result * PRIME + ssin.toBits()
        result = result * PRIME + tx.toBits()
        result = result * PRIME + ty.toBits()
        return result
    }

    override fun toString(): String {
        return "RSXform(_scos=$scos, _ssin=$ssin, _tx=$tx, _ty=$ty)"
    }

    companion object {
        /**
         * Initialize a new xform based on the scale, rotation (in radians), final tx,ty location
         * and anchor-point ax,ay within the src quad.
         *
         * Note: the anchor point is not normalized (e.g. 0...1) but is in pixels of the src image.
         */
        fun makeFromRadians(scale: Float, radians: Float, tx: Float, ty: Float, ax: Float, ay: Float): RSXform {
            val s = sin(radians.toDouble()).toFloat() * scale
            val c = cos(radians.toDouble()).toFloat() * scale
            return RSXform(c, s, tx + -c * ax + s * ay, ty + -s * ax - c * ay)
        }
    }
}