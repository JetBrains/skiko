package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.InteropScope
import org.jetbrains.skia.impl.InteropScopeContext
import org.jetbrains.skia.impl.withResult
import kotlin.jvm.JvmStatic
import kotlin.math.abs

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
        return if (becomesRect) makeLTRB(
            left - spread,
            top - spread,
            maxOf(left - spread, right + spread),
            maxOf(top - spread, bottom + spread)
        ) else {
            val radii = radii.copyOf()
            for (i in radii.indices) radii[i] = maxOf(0.0f, radii[i] + spread)
            RRect(
                left - spread,
                top - spread,
                maxOf(left - spread, right + spread),
                maxOf(top - spread, bottom + spread),
                radii
            )
        }
    }

    override fun toString(): String {
        return "RRect(_left=$left, _top=$top, _right=$right, _bottom=$bottom, _radii=${radii.joinToString()})"
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is RRect) return false
        if (!super.equals(other)) return false
        return if (radii.size == other.radii.size) {
            radii.contentEquals(other.radii)
        } else {
            normalizeRadii(radii).contentEquals(normalizeRadii(other.radii))
        }
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = super.hashCode()
        result = result * PRIME + radii.contentHashCode()
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
            return RRect(l, t, r, b, floatArrayOf(abs(r - l) / 2.0f, abs(b - t) / 2.0f))
        }

        fun makePillLTRB(l: Float, t: Float, r: Float, b: Float): RRect {
            return RRect(l, t, r, b, floatArrayOf(minOf(abs(r - l), abs(t - b)) / 2.0f))
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
            return RRect(l, t, l + w, t + h, floatArrayOf(minOf(w, h) / 2.0f))
        }

        internal fun fromInteropPointer(block: InteropScopeContext): RRect {
            val result = withResult(FloatArray(12 /* 4 dimensions + 4 radii * 2 */), block)
            return RRect(result[0], result[1], result[2], result[3], result.copyOfRange(4, 12))
        }

        internal fun fromInteropPointerNullable(block: (InteropPointer) -> Boolean): RRect? {
            var result = true
            val rect = fromInteropPointer { result = block(it) }
            return if (result) { rect } else { null }
        }
    }
}

private fun normalizeRadii(radii: FloatArray): FloatArray {
    return when (radii.size) {
        0 -> FloatArray(8) { 0.0f }
        1 -> FloatArray(8) { radii[0] }      // All are the same
        2 -> FloatArray(8) { radii[it % 2] } // Corresponding values are same for each corner
        4 -> FloatArray(8) { radii[it / 2] } // Horizontal and vertical are same for each corner
        8 -> radii
        else -> throw Error("illegal radii array")
    }
}