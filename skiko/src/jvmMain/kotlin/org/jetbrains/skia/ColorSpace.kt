package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.Stats
import java.lang.ref.Reference

class ColorSpace : Managed {
    companion object {
        @JvmStatic external fun _nGetFinalizer(): Long
        @JvmStatic external fun _nMakeSRGB(): Long
        @JvmStatic external fun _nMakeDisplayP3(): Long
        @JvmStatic external fun _nMakeSRGBLinear(): Long
        @JvmStatic external fun _nConvert(fromPtr: Long, toPtr: Long, r: Float, g: Float, b: Float, a: Float): FloatArray
        @JvmStatic external fun _nIsGammaCloseToSRGB(ptr: Long): Boolean
        @JvmStatic external fun _nIsGammaLinear(ptr: Long): Boolean
        @JvmStatic external fun _nIsSRGB(ptr: Long): Boolean

        init {
            staticLoad()
        }

        val sRGB = ColorSpace(_nMakeSRGB(), false)
        val displayP3 = ColorSpace(_nMakeDisplayP3(), false)
    }


    fun convert(to: ColorSpace?, color: Color4f): Color4f {
        var to = to
        to = to ?: sRGB
        return try {
            Color4f(
                _nConvert(
                    _ptr,
                    getPtr(to),
                    color.r,
                    color.g,
                    color.b,
                    color.a
                )
            )
        } finally {
            Reference.reachabilityFence(this)
            Reference.reachabilityFence(to)
        }
    }

    internal constructor(ptr: Long) : super(ptr, _FinalizerHolder.PTR, true)

    internal constructor(ptr: Long, managed: Boolean) : super(ptr, _FinalizerHolder.PTR, managed)

    /**
     * @return  true if the color space gamma is near enough to be approximated as sRGB
     */
    val isGammaCloseToSRGB: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsGammaCloseToSRGB(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     * @return  true if the color space gamma is linear
     */
    val isGammaLinear: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsGammaLinear(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    /**
     *
     * Returns true if the color space is sRGB. Returns false otherwise.
     *
     *
     * This allows a little bit of tolerance, given that we might see small numerical error
     * in some cases: converting ICC fixed point to float, converting white point to D50,
     * rounding decisions on transfer function and matrix.
     *
     *
     * This does not consider a 2.2f exponential transfer function to be sRGB.  While these
     * functions are similar (and it is sometimes useful to consider them together), this
     * function checks for logical equality.
     */
    val isSRGB: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsSRGB(_ptr)
        } finally {
            Reference.reachabilityFence(this)
        }

    private object _FinalizerHolder {
        val PTR = _nGetFinalizer()
    }
}