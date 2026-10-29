package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad
import kotlin.jvm.JvmStatic

/**
 *  Shaders specify the source color(s) for what is being drawn. If a paint
 *  has no shader, then the paint's color is used. If the paint has a
 *  shader, then the shader's color(s) are use instead, but they are
 *  modulated by the paint's alpha. This makes it easy to create a shader
 *  once (e.g. bitmap tiling or gradient) and then change its transparency
 *  w/o having to modify the original shader... only the paint's alpha needs
 *  to be modified.
 */
class Shader internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        init {
            staticLoad()
        }

        /**
         * Returns a shader that generates a linear gradient between the two specified points.
         * If the inputs are invalid, this will return nullptr.
         * @param points  Array of 2 points, the end-points of the line segment
         * @param gradient Description of the colors and interpolation method
         * @param localMatrix Optional local matrix, may be null
         */
        @JvmStatic
        fun makeLinearGradient(
            p0: Point,
            p1: Point,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader = makeLinearGradient(p0.x, p0.y, p1.x, p1.y, gradient, localMatrix)

        @JvmStatic
        fun makeLinearGradient(
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader = nativeCallFromStatic(gradient.colors.colorSpace) { colorSpace ->
            Shader(
                interopScope {
                    _nMakeLinearGradient(
                        x0,
                        y0,
                        x1,
                        y1,
                        toInterop(Color4f.flattenArray(gradient.colors.colors)),
                        colorSpace.pointerOrNullPointer,
                        toInterop(gradient.colors.positions),
                        gradient.colors.colors.size,
                        gradient.colors.tileMode.ordinal,
                        gradient.interpolation.inPremul.ordinal,
                        gradient.interpolation.colorSpace.ordinal,
                        gradient.interpolation.hueMethod.ordinal,
                        toInterop(localMatrix?.mat)
                    )
                }
            )
        }

        /**
         * Returns a shader that generates a radial gradient given the center and radius.
         * @param center  The center of the circle for this gradient
         * @param radius  Must be positive. The radius of the circle for this gradient
         * @param gradient Description of the colors and interpolation method
         * @param localMatrix Optional local matrix, may be null
         */
        @JvmStatic
        fun makeRadialGradient(
            center: Point,
            radius: Float,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader {
            return makeRadialGradient(center.x, center.y, radius, gradient, localMatrix)
        }

        @JvmStatic
        fun makeRadialGradient(
            x: Float,
            y: Float,
            radius: Float,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader = nativeCallFromStatic(gradient.colors.colorSpace) { colorSpace ->
            Shader(
                interopScope {
                    _nMakeRadialGradient(
                        x,
                        y,
                        radius,
                        toInterop(Color4f.flattenArray(gradient.colors.colors)),
                        colorSpace.pointerOrNullPointer,
                        toInterop(gradient.colors.positions),
                        gradient.colors.colors.size,
                        gradient.colors.tileMode.ordinal,
                        gradient.interpolation.inPremul.ordinal,
                        gradient.interpolation.colorSpace.ordinal,
                        gradient.interpolation.hueMethod.ordinal,
                        toInterop(localMatrix?.mat)
                    )
                }
            )
        }

        /**
         * Returns a shader that generates a conical gradient given two circles, or
         * returns null if the inputs are invalid. The gradient interprets the
         * two circles according to the following HTML spec.
         * http://dev.w3.org/html5/2dcontext/#dom-context-2d-createradialgradient
         * @param start        The center of the circle for this gradient
         * @param startRadius  Must be positive. The radius of the circle for this gradient
         * @param end          The center of the circle for this gradient
         * @param endRadius    Must be positive. The radius of the circle for this gradient
         * @param gradient     Description of the colors and interpolation method
         * @param localMatrix  Optional local matrix, may be null
         */
        @JvmStatic
        fun makeTwoPointConicalGradient(
            start: Point,
            startRadius: Float,
            end: Point,
            endRadius: Float,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader {
            return makeTwoPointConicalGradient(
                start.x,
                start.y,
                startRadius,
                end.x,
                end.y,
                endRadius,
                gradient,
                localMatrix
            )
        }

        @JvmStatic
        fun makeTwoPointConicalGradient(
            x0: Float,
            y0: Float,
            startRadius: Float,
            x1: Float,
            y1: Float,
            endRadius: Float,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader = nativeCallFromStatic(gradient.colors.colorSpace) { colorSpace ->
            Shader(
                interopScope {
                    _nMakeTwoPointConicalGradient(
                        x0,
                        y0,
                        startRadius,
                        x1,
                        y1,
                        endRadius,
                        toInterop(Color4f.flattenArray(gradient.colors.colors)),
                        colorSpace.pointerOrNullPointer,
                        toInterop(gradient.colors.positions),
                        gradient.colors.colors.size,
                        gradient.colors.tileMode.ordinal,
                        gradient.interpolation.inPremul.ordinal,
                        gradient.interpolation.colorSpace.ordinal,
                        gradient.interpolation.hueMethod.ordinal,
                        toInterop(localMatrix?.mat)
                    )
                }
            )
        }

        /**
         * Returns a shader that generates a sweep gradient given a center.
         * The shader accepts negative angles and angles larger than 360, draws
         * between 0 and 360 degrees, similar to the CSS conic-gradient
         * semantics. 0 degrees means horizontal positive x axis. The start angle
         * must be less than the end angle, otherwise a null pointer is
         * returned. If color stops do not contain 0 and 1 but are within this
         * range, the respective outer color stop is repeated for 0 and 1. Color
         * stops less than 0 are clamped to 0, and greater than 1 are clamped to 1.
         * @param center      The center of the sweep
         * @param gradient    Description of the colors and interpolation method
         * @param localMatrix Optional local matrix, may be null
         */
        @JvmStatic
        fun makeSweepGradient(
            center: Point,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader {
            return makeSweepGradient(center.x, center.y, gradient, localMatrix)
        }

        @JvmStatic
        fun makeSweepGradient(
            x: Float,
            y: Float,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader {
            return makeSweepGradient(x, y, 0f, 360f, gradient, localMatrix)
        }

        /**
         * Returns a shader that generates a sweep gradient given a center.
         * The shader accepts negative angles and angles larger than 360, draws
         * between 0 and 360 degrees, similar to the CSS conic-gradient
         * semantics. 0 degrees means horizontal positive x axis. The start angle
         * must be less than the end angle, otherwise a null pointer is
         * returned. If color stops do not contain 0 and 1 but are within this
         * range, the respective outer color stop is repeated for 0 and 1. Color
         * stops less than 0 are clamped to 0, and greater than 1 are clamped to 1.
         * @param center      The center of the sweep
         * @param startAngle  Start of the angular range, corresponding to pos == 0.
         * @param endAngle    End of the angular range, corresponding to pos == 1.
         * @param gradient    Description of the colors and interpolation method
         * @param localMatrix Optional local matrix, may be null
         */
        @JvmStatic
        fun makeSweepGradient(
            center: Point,
            startAngle: Float,
            endAngle: Float,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader {
            return makeSweepGradient(center.x, center.y, startAngle, endAngle, gradient, localMatrix)
        }

        @JvmStatic
        fun makeSweepGradient(
            x: Float,
            y: Float,
            startAngle: Float,
            endAngle: Float,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader = nativeCallFromStatic(gradient.colors.colorSpace) { colorSpace ->
            Shader(
                interopScope {
                    _nMakeSweepGradient(
                        x,
                        y,
                        startAngle,
                        endAngle,
                        toInterop(Color4f.flattenArray(gradient.colors.colors)),
                        colorSpace.pointerOrNullPointer,
                        toInterop(gradient.colors.positions),
                        gradient.colors.colors.size,
                        gradient.colors.tileMode.ordinal,
                        gradient.interpolation.inPremul.ordinal,
                        gradient.interpolation.colorSpace.ordinal,
                        gradient.interpolation.hueMethod.ordinal,
                        toInterop(localMatrix?.mat)
                    )
                }
            )
        }

        @JvmStatic
        fun makeEmpty(): Shader = nativeCallFromStatic { Shader(Shader_nMakeEmpty()) }

        @JvmStatic
        fun makeColor(color: Int): Shader = nativeCallFromStatic { Shader(_nMakeColor(color)) }

        @JvmStatic
        fun makeColor(color: Color4f, space: ColorSpace?): Shader = nativeCallFromStatic(space) { space ->
            Shader(
                _nMakeColorCS(
                    color.r,
                    color.g,
                    color.b,
                    color.a,
                    space.pointerOrNullPointer
                )
            )
        }

        @JvmStatic
        fun makeBlend(mode: BlendMode, dst: Shader?, src: Shader?): Shader = nativeCallFromStatic(dst, src) { dst, src ->
            Shader(
                _nMakeBlend(
                    mode.ordinal,
                    dst.pointerOrNullPointer,
                    src.pointerOrNullPointer
                )
            )
        }

        @JvmStatic
        fun makeFractalNoise(
            baseFrequencyX: Float,
            baseFrequencyY: Float,
            numOctaves: Int,
            seed: Float,
            tileSize: ISize = ISize.makeEmpty()
        ): Shader = nativeCallFromStatic {
            Shader(
                interopScope {
                    _nMakeFractalNoise(baseFrequencyX, baseFrequencyY, numOctaves, seed, tileSize.width, tileSize.height)
                }
            )
        }

        @JvmStatic
        fun makeTurbulence(
            baseFrequencyX: Float,
            baseFrequencyY: Float,
            numOctaves: Int,
            seed: Float,
            tileSize: ISize = ISize.makeEmpty()
        ): Shader = nativeCallFromStatic {
            Shader(
                interopScope {
                    _nMakeTurbulence(baseFrequencyX, baseFrequencyY, numOctaves, seed, tileSize.width, tileSize.height)
                }
            )
        }
    }

    /**
     *  Return a shader that will apply the specified localMatrix to this shader.
     *  The specified matrix will be applied before any matrix associated with this shader.
     */
    fun makeWithLocalMatrix(localMatrix: Matrix33): Shader = nativeCall {
        Shader(
            interopScope {
                _nMakeWithLocalMatrix(_ptr, toInterop(localMatrix.mat))
            }
        )
    }

    /**
     *  Create a new shader that produces the same colors as invoking this shader and then applying
     *  the colorfilter.
     */
    fun makeWithColorFilter(filter: ColorFilter?): Shader = nativeCall(filter) { filter ->
        Shader(_nMakeWithColorFilter(_ptr, filter.pointerOrNullPointer))
    }
}

// <editor-fold defaultstate="collapsed" desc="Native External Functions">
@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeEmpty")
// No ForceNativeThreadState: allocates an empty shader that has no fields or references, so it is short and cannot
// block.
// Native call path: SkShaders::Empty
private external fun Shader_nMakeEmpty(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeWithLocalMatrix")
// No ForceNativeThreadState: wraps the shader in a local-matrix shader that takes a reference to it, so it is short and
// cannot block.
// Native call path: SkShader::makeWithLocalMatrix
private external fun _nMakeWithLocalMatrix(ptr: NativePointer, localMatrix: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeWithColorFilter")
// No ForceNativeThreadState: wraps a shader and a color filter in a new shader that holds references to both, so it is
// short and cannot block.
// Native call path: SkShader::makeWithColorFilter -> SkColorFilterShader::Make
private external fun _nMakeWithColorFilter(ptr: NativePointer, colorFilterPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeLinearGradient")
// Needs ForceNativeThreadState: copies and scans every color stop of the gradient, so its time grows with the number of
// stops.
// Native call path: SkShaders::LinearGradient -> SkGradientBaseShader::SkGradientBaseShader
@ForceNativeThreadState
private external fun _nMakeLinearGradient(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    colors: InteropPointer,
    colorSpacePtr: NativePointer,
    positions: InteropPointer,
    count: Int,
    tileMode: Int,
    inPremul: Int,
    interpolationColorSpace: Int,
    hueMethod: Int,
    matrix: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeRadialGradient")
// Needs ForceNativeThreadState: copies and scans every color stop of the gradient, so its time grows with the number of
// stops.
// Native call path: SkShaders::RadialGradient -> SkGradientBaseShader::SkGradientBaseShader
@ForceNativeThreadState
private external fun _nMakeRadialGradient(
    x: Float,
    y: Float,
    radius: Float,
    colors: InteropPointer,
    colorSpacePtr: NativePointer,
    positions: InteropPointer,
    count: Int,
    tileMode: Int,
    inPremul: Int,
    interpolationColorSpace: Int,
    hueMethod: Int,
    matrix: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradient")
// Needs ForceNativeThreadState: copies and scans every color stop of the gradient, so its time grows with the number of
// stops.
// Native call path: SkShaders::TwoPointConicalGradient -> SkGradientBaseShader::SkGradientBaseShader
@ForceNativeThreadState
private external fun _nMakeTwoPointConicalGradient(
    x0: Float,
    y0: Float,
    startRadius: Float,
    x1: Float,
    y1: Float,
    endRadius: Float,
    colors: InteropPointer,
    colorSpacePtr: NativePointer,
    positions: InteropPointer,
    count: Int,
    tileMode: Int,
    inPremul: Int,
    interpolationColorSpace: Int,
    hueMethod: Int,
    matrix: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeSweepGradient")
// Needs ForceNativeThreadState: copies and scans every color stop of the gradient, so its time grows with the number of
// stops.
// Native call path: SkShaders::SweepGradient -> SkGradientBaseShader::SkGradientBaseShader
@ForceNativeThreadState
private external fun _nMakeSweepGradient(
    x: Float,
    y: Float,
    startAngle: Float,
    endAngle: Float,
    colors: InteropPointer,
    colorSpacePtr: NativePointer,
    positions: InteropPointer,
    count: Int,
    tileMode: Int,
    inPremul: Int,
    interpolationColorSpace: Int,
    hueMethod: Int,
    matrix: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeFractalNoise")
// Needs ForceNativeThreadState: with zero octaves it uses a shared static color space, which may wait for another
// thread that is still initializing it.
// Native call path: SkShaders::MakeFractalNoise -> sk_srgb_singleton -> __cxa_guard_acquire
@ForceNativeThreadState
private external fun _nMakeFractalNoise(
    baseFrequencyX: Float,
    baseFrequencyY: Float,
    numOctaves: Int,
    seed: Float,
    tileWidth: Int,
    tileHeight: Int,
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeTurbulence")
// Needs ForceNativeThreadState: with zero octaves it uses a shared static color space, which may wait for another
// thread that is still initializing it.
// Native call path: SkShaders::MakeTurbulence -> sk_srgb_singleton -> __cxa_guard_acquire
@ForceNativeThreadState
private external fun _nMakeTurbulence(
    baseFrequencyX: Float,
    baseFrequencyY: Float,
    numOctaves: Int,
    seed: Float,
    tileWidth: Int,
    tileHeight: Int,
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeColor")
// Needs ForceNativeThreadState: uses a shared static sRGB color space that may wait for another thread initializing it
// on first use.
// Native call path: SkShaders::Color -> SkColorSpace::MakeSRGB -> sk_srgb_singleton
@ForceNativeThreadState
private external fun _nMakeColor(color: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeColorCS")
// Needs ForceNativeThreadState: computes lazy color space fields behind a one-time guard that may spin-wait for another
// thread.
// Native call path: SkShaders::Color -> SkColorSpace::computeLazyDstFields -> SkOnce::operator()
@ForceNativeThreadState
private external fun _nMakeColorCS(r: Float, g: Float, b: Float, a: Float, colorSpacePtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeBlend")
// Needs ForceNativeThreadState: a clear blend uses a shared static sRGB color space that may wait for another thread
// initializing it.
// Native call path: SkShaders::Blend -> SkShaders::Color -> sk_srgb_singleton
@ForceNativeThreadState
private external fun _nMakeBlend(blendMode: Int, dst: NativePointer, src: NativePointer): NativePointer
// </editor-fold>
