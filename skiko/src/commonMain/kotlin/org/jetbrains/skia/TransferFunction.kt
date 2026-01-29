package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

/**
 * A transfer function that maps encoded values to linear values,
 * represented by this 7-parameter piecewise function:
 *
 *     linear = sign(encoded) *  (c*|encoded| + f)       , 0 <= |encoded| < d
 *            = sign(encoded) * ((a*|encoded| + b)^g + e), d <= |encoded|
 *
 * A simple gamma transfer function sets g = gamma, a = 1, and the rest = 0.
 */
class TransferFunction(
    val g: Float,
    val a: Float,
    val b: Float,
    val c: Float,
    val d: Float,
    val e: Float,
    val f: Float
) {

    companion object {

        init {
            staticLoad()
        }

        val sRGB = TransferFunction(withResult(FloatArray(7)) { _nGetSRGB(it) })
        val gamma2Dot2 = TransferFunction(withResult(FloatArray(7)) { _nGetGamma2Dot2(it) })
        val linear = TransferFunction(withResult(FloatArray(7)) { _nGetLinear(it) })
        val rec2020 = TransferFunction(withResult(FloatArray(7)) { _nGetRec2020(it) })
        val pq = TransferFunction(withResult(FloatArray(7)) { _nGetPQ(it) })
        val hlg = TransferFunction(withResult(FloatArray(7)) { _nGetHLG(it) })

        /**
         * General form of the SMPTE ST 2084 PQ function.
         *
         *                               max(A + B|encoded|^C, 0)
         *     linear = sign(encoded) * (------------------------) ^ F
         *                                   D + E|encoded|^C
         */
        fun makePQish(A: Float, B: Float, C: Float, D: Float, E: Float, F: Float): TransferFunction {
            Stats.onNativeCall()
            return TransferFunction(withResult(FloatArray(7)) {
                _nMakePQish(A, B, C, D, E, F, it)
            })
        }

        /**
         * General form of HLG.
         *
         *              { K * sign(encoded) * ( (R|encoded|)^G )          when 0   <= |encoded| <= 1/R
         *     linear = { K * sign(encoded) * ( e^(a(|encoded|-c)) + b )  when 1/R <  |encoded|
         */
        fun makeScaledHLGish(K: Float, R: Float, G: Float, a: Float, b: Float, c: Float): TransferFunction {
            Stats.onNativeCall()
            return TransferFunction(withResult(FloatArray(7)) {
                _nMakeScaledHLGish(K, R, G, a, b, c, it)
            })
        }

    }

    val type: TransferFunctionType
        get() = try {
            Stats.onNativeCall()
            TransferFunctionType.values()[interopScope {
                _nGetType(toInterop(asArray()))
            }]
        } finally {
            reachabilityBarrier(this)
        }

    fun eval(x: Float): Float {
        return try {
            Stats.onNativeCall()
            interopScope {
                _nEval(toInterop(asArray()), x)
            }
        } finally {
            reachabilityBarrier(this)
        }
    }

    fun invert(): TransferFunction? {
        return try {
            Stats.onNativeCall()
            withNullableResult(FloatArray(7)) {
                _nInvert(toInterop(asArray()), it)
            }?.let { TransferFunction(it) }
        } finally {
            reachabilityBarrier(this)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is TransferFunction) return false
        if (g != other.g) return false
        if (a != other.a) return false
        if (b != other.b) return false
        if (c != other.c) return false
        if (d != other.d) return false
        if (e != other.e) return false
        return f == other.f
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = 1
        result = result * PRIME + g.toBits()
        result = result * PRIME + a.toBits()
        result = result * PRIME + b.toBits()
        result = result * PRIME + c.toBits()
        result = result * PRIME + d.toBits()
        result = result * PRIME + e.toBits()
        result = result * PRIME + f.toBits()
        return result
    }

    override fun toString(): String {
        return "TransferFunction(_g=$g, _a=$a, _b=$b, _c=$c, _d=$d, _e=$e, _f=$f)"
    }

    internal constructor(array: FloatArray) : this(array[0], array[1], array[2], array[3], array[4], array[5], array[6])

    internal fun asArray() = floatArrayOf(g, a, b, c, d, e, f)

}

@ExternalSymbolName("org_jetbrains_skia_TransferFunction__1nGetSRGB")
private external fun _nGetSRGB(result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TransferFunction__1nGetGamma2Dot2")
private external fun _nGetGamma2Dot2(result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TransferFunction__1nGetLinear")
private external fun _nGetLinear(result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TransferFunction__1nGetRec2020")
private external fun _nGetRec2020(result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TransferFunction__1nGetPQ")
private external fun _nGetPQ(result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TransferFunction__1nGetHLG")
private external fun _nGetHLG(result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TransferFunction__1nMakePQish")
private external fun _nMakePQish(A: Float, B: Float, C: Float, D: Float, E: Float, F: Float, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_TransferFunction__1nMakeScaledHLGish")
private external fun _nMakeScaledHLGish(
    K: Float, R: Float, G: Float, a: Float, b: Float, c: Float, result: InteropPointer
)

@ExternalSymbolName("org_jetbrains_skia_TransferFunction__1nGetType")
private external fun _nGetType(transferFunction: InteropPointer): Int

@ExternalSymbolName("org_jetbrains_skia_TransferFunction__1nEval")
private external fun _nEval(transferFunction: InteropPointer, x: Float): Float

@ExternalSymbolName("org_jetbrains_skia_TransferFunction__1nInvert")
private external fun _nInvert(transferFunction: InteropPointer, result: InteropPointer): Boolean
