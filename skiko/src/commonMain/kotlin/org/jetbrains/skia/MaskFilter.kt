package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import kotlin.jvm.JvmStatic

/**
 * MaskFilter is the base class for object that perform transformations on
 * the mask before drawing it. An example subclass is Blur.
 */
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
        @JvmStatic
        fun makeBlur(mode: FilterBlurMode, sigma: Float, respectCTM: Boolean = true): MaskFilter = nativeCallFromStatic {
            MaskFilter(_nMakeBlur(mode.ordinal, sigma, respectCTM))
        }

        @JvmStatic
        fun makeShader(s: Shader?): MaskFilter = nativeCallFromStatic(s) { s ->
            MaskFilter(_nMakeShader(s.pointerOrNullPointer))
        }

        @JvmStatic
        fun makeTable(table: ByteArray): MaskFilter = nativeCallFromStatic {
            require(table.size == 256) { "Expected 256 elements, got " + table.size }
            MaskFilter(
                interopScope {
                    MaskFilter_nMakeTable(toInterop(table))
                }
            )
        }

        @JvmStatic
        fun makeGamma(gamma: Float): MaskFilter = nativeCallFromStatic {
            MaskFilter(_nMakeGamma(gamma))
        }

        @JvmStatic
        fun makeClip(min: Int, max: Int): MaskFilter = nativeCallFromStatic {
            MaskFilter(_nMakeClip(min.toByte(), max.toByte()))
        }

        // If radius > 0, return the corresponding sigma, else return 0
        @JvmStatic
        fun convertRadiusToSigma(radius: Float) =
            if (radius > 0) kBLUR_SIGMA_SCALE * radius + 0.5f else 0.0f

        // If sigma > 0.5, return the corresponding radius, else return 0
        @JvmStatic
        fun convertSigmaToRadius(sigma: Float) =
            if (sigma > 0.5f) (sigma - 0.5f) / kBLUR_SIGMA_SCALE else 0.0f
    }
}

// A copy from SkBlurMask.cpp
// This constant approximates the scaling done in the software path's
// "high quality" mode, in SkBlurMask::Blur() (1 / sqrt(3)).
private const val kBLUR_SIGMA_SCALE = 0.57735f

// <editor-fold defaultstate="collapsed" desc="Native External Functions">
@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeTable")
// No ForceNativeThreadState: copies a fixed 256-entry table into a new filter object, so it is short and cannot block.
// Native call path: SkTableMaskFilter::Create
private external fun MaskFilter_nMakeTable(table: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeBlur")
// No ForceNativeThreadState: allocates a blur filter object that stores a few plain settings, so it is short and cannot
// block.
// Native call path: SkMaskFilter::MakeBlur
private external fun _nMakeBlur(mode: Int, sigma: Float, respectCTM: Boolean): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeShader")
// No ForceNativeThreadState: stores a reference to a shader in a new filter object, and the caller still holds its own
// reference, so it is short and cannot block.
// Native call path: SkShaderMaskFilter::Make
private external fun _nMakeShader(shaderPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeGamma")
// No ForceNativeThreadState: fills a fixed 256-entry gamma table and copies it into a new filter object, so it is short
// and cannot block.
// Native call path: SkTableMaskFilter::CreateGamma
private external fun _nMakeGamma(gamma: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_MaskFilter__1nMakeClip")
// No ForceNativeThreadState: fills a fixed 256-entry clip table and allocates a new filter object, so it is short and
// cannot block.
// Native call path: SkTableMaskFilter::CreateClip
private external fun _nMakeClip(min: Byte, max: Byte): NativePointer
// </editor-fold>