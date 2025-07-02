package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.impl.withResult

class ColorSpace : Managed {
    companion object {

        init {
            staticLoad()
        }

        val sRGB = ColorSpace(ColorSpace_nMakeSRGB(), false)
        val sRGBLinear = ColorSpace(ColorSpace_nMakeSRGBLinear(), false)
        val displayP3 = ColorSpace(ColorSpace_nMakeDisplayP3(), false)
    }


    fun convert(toColor: ColorSpace?, color: Color4f): Color4f {
        var to = toColor
        to = to ?: sRGB
        return try {
            Color4f(withResult(FloatArray(4)) {
                    ColorSpace_nConvert(
                        _ptr,
                        getPtr(to),
                        color.r,
                        color.g,
                        color.b,
                        color.a,
                        it
                    )
            })
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(to)
        }
    }

    internal constructor(ptr: NativePointer) : super(ptr, _FinalizerHolder.PTR, true)

    internal constructor(ptr: NativePointer, managed: Boolean) : super(ptr, _FinalizerHolder.PTR, managed)

    /**
     * @return  true if the color space gamma is near enough to be approximated as sRGB
     */
    val isGammaCloseToSRGB: Boolean
        get() = try {
            Stats.onNativeCall()
            ColorSpace_nIsGammaCloseToSRGB(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  true if the color space gamma is linear
     */
    val isGammaLinear: Boolean
        get() = try {
            Stats.onNativeCall()
            ColorSpace_nIsGammaLinear(_ptr)
        } finally {
            reachabilityBarrier(this)
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
            ColorSpace_nIsSRGB(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    private object _FinalizerHolder {
        val PTR = ColorSpace_nGetFinalizer()
    }
}