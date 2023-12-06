package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.*

class ColorSpace : Managed {
    companion object {

        init {
            staticLoad()
        }

        val sRGB = ColorSpace(_nMakeSRGB(), false)
        val sRGBLinear = ColorSpace(_nMakeSRGBLinear(), false)
        val displayP3 = ColorSpace(_nMakeDisplayP3(), false)
    }


    fun convert(toColor: ColorSpace?, color: Color4f): Color4f {
        var to = toColor
        to = to ?: sRGB
        return try {
            Color4f(withResult(FloatArray(4)) {
                    _nConvert(
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
            _nIsGammaCloseToSRGB(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    /**
     * @return  true if the color space gamma is linear
     */
    val isGammaLinear: Boolean
        get() = try {
            Stats.onNativeCall()
            _nIsGammaLinear(_ptr)
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
            _nIsSRGB(_ptr)
        } finally {
            reachabilityBarrier(this)
        }

    private object _FinalizerHolder {
        val PTR = ColorSpace_nGetFinalizer()
    }
}

@ExternalSymbolName("org_jetbrains_skia_ColorSpace__1nGetFinalizer")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorSpace__1nGetFinalizer")
private external fun ColorSpace_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorSpace__nConvert")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorSpace__nConvert")
private external fun _nConvert(fromPtr: NativePointer, toPtr: NativePointer, r: Float, g: Float, b: Float, a: Float, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_ColorSpace__1nMakeSRGB")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorSpace__1nMakeSRGB")
private external fun _nMakeSRGB(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorSpace__1nMakeDisplayP3")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorSpace__1nMakeDisplayP3")
private external fun _nMakeDisplayP3(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorSpace__1nMakeSRGBLinear")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorSpace__1nMakeSRGBLinear")
private external fun _nMakeSRGBLinear(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorSpace__1nIsGammaCloseToSRGB")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorSpace__1nIsGammaCloseToSRGB")
private external fun _nIsGammaCloseToSRGB(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_ColorSpace__1nIsGammaLinear")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorSpace__1nIsGammaLinear")
private external fun _nIsGammaLinear(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_ColorSpace__1nIsSRGB")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorSpace__1nIsSRGB")
private external fun _nIsSRGB(ptr: NativePointer): Boolean
