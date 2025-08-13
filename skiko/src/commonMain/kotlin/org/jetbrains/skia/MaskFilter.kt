package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class MaskFilter internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        init {
            staticLoad()
        }

        /**
         * Create a blur [MaskFilter].
         *
         *  @param mode The [FilterBlurMode] to use.
         *  @param sigma Standard deviation of the Gaussian blur to apply. Must be > 0.
         *  @param respectCTM if `true` the blur's sigma is modified by the CTM.
         *  @return The new blur [MaskFilter]
         */
        fun makeBlur(mode: FilterBlurMode, sigma: Float, respectCTM: Boolean = true): MaskFilter {
            Stats.onNativeCall()
            return MaskFilter(_nMakeBlur(mode.ordinal, sigma, respectCTM))
        }

        fun makeShader(s: Shader?): MaskFilter {
            return try {
                Stats.onNativeCall()
                MaskFilter(_nMakeShader(getPtr(s)))
            } finally {
                reachabilityBarrier(s)
            }
        }

        fun makeTable(table: ByteArray): MaskFilter {
            require(table.size == 256) { "Expected 256 elements, got " + table.size }

            Stats.onNativeCall()
            return MaskFilter(
                interopScope {
                    MaskFilter_nMakeTable(toInterop(table))
                }
            )
        }

        fun makeGamma(gamma: Float): MaskFilter {
            Stats.onNativeCall()
            return MaskFilter(_nMakeGamma(gamma))
        }

        fun makeClip(min: Int, max: Int): MaskFilter {
            Stats.onNativeCall()
            return MaskFilter(_nMakeClip(min.toByte(), max.toByte()))
        }

        // If radius > 0, return the corresponding sigma, else return 0
        fun convertRadiusToSigma(radius: Float) =
            if (radius > 0) kBLUR_SIGMA_SCALE * radius + 0.5f else 0.0f

        // If sigma > 0.5, return the corresponding radius, else return 0
        fun convertSigmaToRadius(sigma: Float) =
            if (sigma > 0.5f) (sigma - 0.5f) / kBLUR_SIGMA_SCALE else 0.0f
    }
}

// A copy from SkBlurMask.cpp
// This constant approximates the scaling done in the software path's
// "high quality" mode, in SkBlurMask::Blur() (1 / sqrt(3)).
private const val kBLUR_SIGMA_SCALE = 0.57735f

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeTable")
private external fun MaskFilter_nMakeTable(table: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeBlur")
private external fun _nMakeBlur(mode: Int, sigma: Float, respectCTM: Boolean): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeShader")
private external fun _nMakeShader(shaderPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeGamma")
private external fun _nMakeGamma(gamma: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeClip")
private external fun _nMakeClip(min: Byte, max: Byte): NativePointer
