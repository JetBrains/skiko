package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

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
        fun makeLinearGradient(
            p0: Point,
            p1: Point,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader {
            return makeLinearGradient(p0.x, p0.y, p1.x, p1.y, gradient, localMatrix)
        }

        fun makeLinearGradient(
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader {
            return try {
                Stats.onNativeCall()
                Shader(
                    interopScope {
                        _nMakeLinearGradient(
                            x0,
                            y0,
                            x1,
                            y1,
                            toInterop(Color4f.flattenArray(gradient.colors.colors)),
                            getPtr(gradient.colors.colorSpace),
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
            } finally {
                reachabilityBarrier(gradient)
                reachabilityBarrier(gradient.colors.colorSpace)
                reachabilityBarrier(localMatrix)
            }
        }

        /**
         * Returns a shader that generates a radial gradient given the center and radius.
         * @param center  The center of the circle for this gradient
         * @param radius  Must be positive. The radius of the circle for this gradient
         * @param gradient Description of the colors and interpolation method
         * @param localMatrix Optional local matrix, may be null
         */
        fun makeRadialGradient(
            center: Point,
            radius: Float,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader {
            return makeRadialGradient(center.x, center.y, radius, gradient, localMatrix)
        }

        fun makeRadialGradient(
            x: Float,
            y: Float,
            radius: Float,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader {
            return try {
                Stats.onNativeCall()
                Shader(
                    interopScope {
                        _nMakeRadialGradient(
                            x,
                            y,
                            radius,
                            toInterop(Color4f.flattenArray(gradient.colors.colors)),
                            getPtr(gradient.colors.colorSpace),
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
            } finally {
                reachabilityBarrier(gradient)
                reachabilityBarrier(gradient.colors.colorSpace)
                reachabilityBarrier(localMatrix)
            }
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

        fun makeTwoPointConicalGradient(
            x0: Float,
            y0: Float,
            startRadius: Float,
            x1: Float,
            y1: Float,
            endRadius: Float,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader {
            return try {
                Stats.onNativeCall()
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
                            getPtr(gradient.colors.colorSpace),
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
            } finally {
                reachabilityBarrier(gradient)
                reachabilityBarrier(gradient.colors.colorSpace)
                reachabilityBarrier(localMatrix)
            }
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
        fun makeSweepGradient(
            center: Point,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader {
            return makeSweepGradient(center.x, center.y, gradient, localMatrix)
        }

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
        fun makeSweepGradient(
            center: Point,
            startAngle: Float,
            endAngle: Float,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader {
            return makeSweepGradient(center.x, center.y, startAngle, endAngle, gradient, localMatrix)
        }

        fun makeSweepGradient(
            x: Float,
            y: Float,
            startAngle: Float,
            endAngle: Float,
            gradient: Gradient,
            localMatrix: Matrix33? = null
        ): Shader {
            return try {
                Stats.onNativeCall()
                Shader(
                    interopScope {
                        _nMakeSweepGradient(
                            x,
                            y,
                            startAngle,
                            endAngle,
                            toInterop(Color4f.flattenArray(gradient.colors.colors)),
                            getPtr(gradient.colors.colorSpace),
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
            } finally {
                reachabilityBarrier(gradient)
                reachabilityBarrier(gradient.colors.colorSpace)
                reachabilityBarrier(localMatrix)
            }
        }

        fun makeEmpty(): Shader {
            Stats.onNativeCall()
            return Shader(Shader_nMakeEmpty())
        }

        fun makeColor(color: Int): Shader {
            Stats.onNativeCall()
            return Shader(_nMakeColor(color))
        }

        fun makeColor(color: Color4f, space: ColorSpace?): Shader {
            return try {
                Stats.onNativeCall()
                Shader(
                    _nMakeColorCS(
                        color.r,
                        color.g,
                        color.b,
                        color.a,
                        getPtr(space)
                    )
                )
            } finally {
                reachabilityBarrier(space)
            }
        }

        fun makeBlend(mode: BlendMode, dst: Shader?, src: Shader?): Shader {
            return try {
                Stats.onNativeCall()
                Shader(
                    _nMakeBlend(
                        mode.ordinal,
                        getPtr(dst),
                        getPtr(src)
                    )
                )
            } finally {
                reachabilityBarrier(dst)
                reachabilityBarrier(src)
            }
        }

        fun makeFractalNoise(
            baseFrequencyX: Float,
            baseFrequencyY: Float,
            numOctaves: Int,
            seed: Float,
            tileSize: ISize = ISize.makeEmpty()
        ): Shader {
            return try {
                Stats.onNativeCall()
                Shader(
                    interopScope {
                        _nMakeFractalNoise(baseFrequencyX, baseFrequencyY, numOctaves, seed, tileSize.width, tileSize.height)
                    }
                )
            } finally {
                reachabilityBarrier(this)
            }
        }

        fun makeTurbulence(
            baseFrequencyX: Float,
            baseFrequencyY: Float,
            numOctaves: Int,
            seed: Float,
            tileSize: ISize = ISize.makeEmpty()
        ): Shader {
            return try {
                Stats.onNativeCall()
                Shader(
                    interopScope {
                        _nMakeTurbulence(baseFrequencyX, baseFrequencyY, numOctaves, seed, tileSize.width, tileSize.height)
                    }
                )
            } finally {
                reachabilityBarrier(this)
            }
        }
    }

    /**
     *  Return a shader that will apply the specified localMatrix to this shader.
     *  The specified matrix will be applied before any matrix associated with this shader.
     */
    fun makeWithLocalMatrix(localMatrix: Matrix33): Shader {
        return try {
            Stats.onNativeCall()
            Shader(
                interopScope {
                    _nMakeWithLocalMatrix(_ptr, toInterop(localMatrix.mat))
                })
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(localMatrix)
        }
    }

    /**
     *  Create a new shader that produces the same colors as invoking this shader and then applying
     *  the colorfilter.
     */
    fun makeWithColorFilter(filter: ColorFilter?): Shader {
        return try {
            Stats.onNativeCall()
            Shader(_nMakeWithColorFilter(_ptr, getPtr(filter)))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(filter)
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeEmpty")
private external fun Shader_nMakeEmpty(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeWithLocalMatrix")
private external fun _nMakeWithLocalMatrix(ptr: NativePointer, localMatrix: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeWithColorFilter")
private external fun _nMakeWithColorFilter(ptr: NativePointer, colorFilterPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeLinearGradient")
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
private external fun _nMakeFractalNoise(
    baseFrequencyX: Float,
    baseFrequencyY: Float,
    numOctaves: Int,
    seed: Float,
    tileWidth: Int,
    tileHeight: Int,
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeTurbulence")
private external fun _nMakeTurbulence(
    baseFrequencyX: Float,
    baseFrequencyY: Float,
    numOctaves: Int,
    seed: Float,
    tileWidth: Int,
    tileHeight: Int,
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeColor")
private external fun _nMakeColor(color: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeColorCS")
private external fun _nMakeColorCS(r: Float, g: Float, b: Float, a: Float, colorSpacePtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeBlend")
private external fun _nMakeBlend(blendMode: Int, dst: NativePointer, src: NativePointer): NativePointer
