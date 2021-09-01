package org.jetbrains.skija

import java.util.*

class RRect internal constructor(l: Float, t: Float, r: Float, b: Float, val radii: FloatArray) :
    Rect(l, t, r, b) {
    override fun inflate(spread: Float): Rect {
        var becomesRect = true
        for (i in radii.indices) {
            if (radii[i] + spread >= 0) {
                becomesRect = false
                break
            }
        }
        return if (becomesRect) Rect.Companion.makeLTRB(
            left - spread,
            top - spread,
            Math.max(left - spread, right + spread),
            Math.max(top - spread, bottom + spread)
        ) else {
            val radii = Arrays.copyOf(radii, radii.size)
            for (i in radii.indices) radii[i] = Math.max(0.0f, radii[i] + spread)
            RRect(
                left - spread,
                top - spread,
                Math.max(left - spread, right + spread),
                Math.max(top - spread, bottom + spread),
                radii
            )
        }
    }

    override fun toString(): String {
        return "RRect(_left=" + left + ", _top=" + top + ", _right=" + right + ", _bottom=" + bottom + ", _radii=" + Arrays.toString(
            radii
        ) + ")"
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is RRect) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (!super.equals(o)) return false
        return if (!Arrays.equals(radii, other.radii)) false else true
    }

    override fun canEqual(other: Any?): Boolean {
        return other is RRect
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = super.hashCode()
        result = result * PRIME + Arrays.hashCode(radii)
        return result
    }

    companion object {
        @JvmStatic
        fun makeLTRB(l: Float, t: Float, r: Float, b: Float, radius: Float): RRect {
            return RRect(l, t, r, b, floatArrayOf(radius))
        }

        @JvmStatic
        fun makeLTRB(l: Float, t: Float, r: Float, b: Float, xRad: Float, yRad: Float): RRect {
            return RRect(l, t, r, b, floatArrayOf(xRad, yRad))
        }

        @JvmStatic
        fun makeLTRB(
            l: Float,
            t: Float,
            r: Float,
            b: Float,
            tlRad: Float,
            trRad: Float,
            brRad: Float,
            blRad: Float
        ): RRect {
            return RRect(l, t, r, b, floatArrayOf(tlRad, trRad, brRad, blRad))
        }

        @JvmStatic
        fun makeNinePatchLTRB(
            l: Float,
            t: Float,
            r: Float,
            b: Float,
            lRad: Float,
            tRad: Float,
            rRad: Float,
            bRad: Float
        ): RRect {
            return RRect(l, t, r, b, floatArrayOf(lRad, tRad, rRad, tRad, rRad, bRad, lRad, bRad))
        }

        @JvmStatic
        fun makeComplexLTRB(l: Float, t: Float, r: Float, b: Float, radii: FloatArray): RRect {
            return RRect(l, t, r, b, radii)
        }

        fun makeOvalLTRB(l: Float, t: Float, r: Float, b: Float): RRect {
            return RRect(l, t, r, b, floatArrayOf(Math.abs(r - l) / 2.0f, Math.abs(b - t) / 2.0f))
        }

        fun makePillLTRB(l: Float, t: Float, r: Float, b: Float): RRect {
            return RRect(l, t, r, b, floatArrayOf(Math.min(Math.abs(r - l), Math.abs(t - b)) / 2.0f))
        }

        fun makeXYWH(l: Float, t: Float, w: Float, h: Float, radius: Float): RRect {
            return RRect(l, t, l + w, t + h, floatArrayOf(radius))
        }

        fun makeXYWH(l: Float, t: Float, w: Float, h: Float, xRad: Float, yRad: Float): RRect {
            return RRect(l, t, l + w, t + h, floatArrayOf(xRad, yRad))
        }

        fun makeXYWH(
            l: Float,
            t: Float,
            w: Float,
            h: Float,
            tlRad: Float,
            trRad: Float,
            brRad: Float,
            blRad: Float
        ): RRect {
            return RRect(l, t, l + w, t + h, floatArrayOf(tlRad, trRad, brRad, blRad))
        }

        fun makeNinePatchXYWH(
            l: Float,
            t: Float,
            w: Float,
            h: Float,
            lRad: Float,
            tRad: Float,
            rRad: Float,
            bRad: Float
        ): RRect {
            return RRect(l, t, l + w, t + h, floatArrayOf(lRad, tRad, rRad, tRad, rRad, bRad, lRad, bRad))
        }

        fun makeComplexXYWH(l: Float, t: Float, w: Float, h: Float, radii: FloatArray): RRect {
            return RRect(l, t, l + w, t + h, radii)
        }

        fun makeOvalXYWH(l: Float, t: Float, w: Float, h: Float): RRect {
            return RRect(l, t, l + w, t + h, floatArrayOf(w / 2.0f, h / 2.0f))
        }

        fun makePillXYWH(l: Float, t: Float, w: Float, h: Float): RRect {
            return RRect(l, t, l + w, t + h, floatArrayOf(Math.min(w, h) / 2.0f))
        }
    }
}