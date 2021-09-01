package org.jetbrains.skija

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

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is RSXform) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (java.lang.Float.compare(scos, other.scos) != 0) return false
        if (java.lang.Float.compare(ssin, other.ssin) != 0) return false
        if (java.lang.Float.compare(tx, other.tx) != 0) return false
        return if (java.lang.Float.compare(ty, other.ty) != 0) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is RSXform
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + java.lang.Float.floatToIntBits(scos)
        result = result * PRIME + java.lang.Float.floatToIntBits(ssin)
        result = result * PRIME + java.lang.Float.floatToIntBits(tx)
        result = result * PRIME + java.lang.Float.floatToIntBits(ty)
        return result
    }

    override fun toString(): String {
        return "RSXform(_scos=" + scos + ", _ssin=" + ssin + ", _tx=" + tx + ", _ty=" + ty + ")"
    }

    companion object {
        /**
         * Initialize a new xform based on the scale, rotation (in radians), final tx,ty location
         * and anchor-point ax,ay within the src quad.
         *
         * Note: the anchor point is not normalized (e.g. 0...1) but is in pixels of the src image.
         */
        fun makeFromRadians(scale: Float, radians: Float, tx: Float, ty: Float, ax: Float, ay: Float): RSXform {
            val s = Math.sin(radians.toDouble()).toFloat() * scale
            val c = Math.cos(radians.toDouble()).toFloat() * scale
            return RSXform(c, s, tx + -c * ax + s * ay, ty + -s * ax - c * ay)
        }
    }
}