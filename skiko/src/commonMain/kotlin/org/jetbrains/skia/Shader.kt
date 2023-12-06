package org.jetbrains.skia

import org.jetbrains.skia.impl.*
import org.jetbrains.skia.impl.Library.Companion.staticLoad

class Shader internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        // Linear
        fun makeLinearGradient(p0: Point, p1: Point, colors: IntArray): Shader {
            return makeLinearGradient(p0.x, p0.y, p1.x, p1.y, colors)
        }

        fun makeLinearGradient(p0: Point, p1: Point, colors: IntArray, positions: FloatArray?): Shader {
            return makeLinearGradient(p0.x, p0.y, p1.x, p1.y, colors, positions)
        }

        fun makeLinearGradient(
            p0: Point,
            p1: Point,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeLinearGradient(p0.x, p0.y, p1.x, p1.y, colors, positions, style)
        }

        fun makeLinearGradient(
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            colors: IntArray,
            positions: FloatArray? = null,
            style: GradientStyle = GradientStyle.Companion.DEFAULT
        ): Shader {
            require(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
            Stats.onNativeCall()
            return Shader(
                interopScope {
                    _nMakeLinearGradient(
                        x0,
                        y0,
                        x1,
                        y1,
                        toInterop(colors),
                        toInterop(positions),
                        colors.size,
                        style.tileMode.ordinal,
                        style._getFlags(),
                        toInterop(style._getMatrixArray())
                    )
                }
            )
        }

        fun makeLinearGradient(
            p0: Point,
            p1: Point,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeLinearGradient(p0.x, p0.y, p1.x, p1.y, colors, cs, positions, style)
        }

        fun makeLinearGradient(
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return try {
                require(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
                Stats.onNativeCall()
                Shader(
                    interopScope {
                        _nMakeLinearGradientCS(
                            x0,
                            y0,
                            x1,
                            y1,
                            toInterop(Color4f.flattenArray(colors)),
                            getPtr(cs),
                            toInterop(positions),
                            colors.size,
                            style.tileMode.ordinal,
                            style._getFlags(),
                            toInterop(style._getMatrixArray())
                        )
                    }
                )
            } finally {
                reachabilityBarrier(cs)
            }
        }

        // Radial
        fun makeRadialGradient(center: Point, r: Float, colors: IntArray): Shader {
            return makeRadialGradient(center.x, center.y, r, colors)
        }

        fun makeRadialGradient(center: Point, r: Float, colors: IntArray, positions: FloatArray?): Shader {
            return makeRadialGradient(center.x, center.y, r, colors, positions)
        }

        fun makeRadialGradient(
            center: Point,
            r: Float,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeRadialGradient(center.x, center.y, r, colors, positions, style)
        }

        fun makeRadialGradient(
            x: Float,
            y: Float,
            r: Float,
            colors: IntArray,
            positions: FloatArray? = null,
            style: GradientStyle = GradientStyle.Companion.DEFAULT
        ): Shader {
            require(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
            Stats.onNativeCall()
            return Shader(
                interopScope {
                    _nMakeRadialGradient(
                        x,
                        y,
                        r,
                        toInterop(colors),
                        toInterop(positions),
                        colors.size,
                        style.tileMode.ordinal,
                        style._getFlags(),
                        toInterop(style._getMatrixArray())
                    )
                }
            )
        }

        fun makeRadialGradient(
            center: Point,
            r: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeRadialGradient(center.x, center.y, r, colors, cs, positions, style)
        }

        fun makeRadialGradient(
            x: Float,
            y: Float,
            r: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return try {
                require(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
                Stats.onNativeCall()
                Shader(
                    interopScope {
                        _nMakeRadialGradientCS(
                            x,
                            y,
                            r,
                            toInterop(Color4f.flattenArray(colors)),
                            getPtr(cs),
                            toInterop(positions),
                            colors.size,
                            style.tileMode.ordinal,
                            style._getFlags(),
                            toInterop(style._getMatrixArray())
                        )
                    }
                )
            } finally {
                reachabilityBarrier(cs)
            }
        }

        // Two-point Conical
        fun makeTwoPointConicalGradient(p0: Point, r0: Float, p1: Point, r1: Float, colors: IntArray): Shader {
            return makeTwoPointConicalGradient(p0.x, p0.y, r0, p1.x, p1.y, r1, colors)
        }

        fun makeTwoPointConicalGradient(
            p0: Point,
            r0: Float,
            p1: Point,
            r1: Float,
            colors: IntArray,
            positions: FloatArray?
        ): Shader {
            return makeTwoPointConicalGradient(p0.x, p0.y, r0, p1.x, p1.y, r1, colors, positions)
        }

        fun makeTwoPointConicalGradient(
            p0: Point,
            r0: Float,
            p1: Point,
            r1: Float,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeTwoPointConicalGradient(p0.x, p0.y, r0, p1.x, p1.y, r1, colors, positions, style)
        }

        fun makeTwoPointConicalGradient(
            x0: Float,
            y0: Float,
            r0: Float,
            x1: Float,
            y1: Float,
            r1: Float,
            colors: IntArray,
            positions: FloatArray? = null,
            style: GradientStyle = GradientStyle.Companion.DEFAULT
        ): Shader {
            require(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
            Stats.onNativeCall()
            return Shader(
                interopScope {
                    _nMakeTwoPointConicalGradient(
                        x0,
                        y0,
                        r0,
                        x1,
                        y1,
                        r1,
                        toInterop(colors),
                        toInterop(positions),
                        colors.size,
                        style.tileMode.ordinal,
                        style._getFlags(),
                        toInterop(style._getMatrixArray())
                    )
                }
            )
        }

        fun makeTwoPointConicalGradient(
            p0: Point,
            r0: Float,
            p1: Point,
            r1: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeTwoPointConicalGradient(p0.x, p0.y, r0, p1.x, p1.y, r1, colors, cs, positions, style)
        }

        fun makeTwoPointConicalGradient(
            x0: Float,
            y0: Float,
            r0: Float,
            x1: Float,
            y1: Float,
            r1: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return try {
                require(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
                Stats.onNativeCall()
                Shader(
                    interopScope {
                        _nMakeTwoPointConicalGradientCS(
                            x0,
                            y0,
                            r0,
                            x1,
                            y1,
                            r1,
                            toInterop(Color4f.flattenArray(colors)),
                            getPtr(cs),
                            toInterop(positions),
                            colors.size,
                            style.tileMode.ordinal,
                            style._getFlags(),
                            toInterop(style._getMatrixArray())
                        )
                    }
                )
            } finally {
                reachabilityBarrier(cs)
            }
        }

        // Sweep
        fun makeSweepGradient(center: Point, colors: IntArray): Shader {
            return makeSweepGradient(center.x, center.y, colors)
        }

        fun makeSweepGradient(x: Float, y: Float, colors: IntArray): Shader {
            return makeSweepGradient(x, y, 0f, 360f, colors, null, GradientStyle.Companion.DEFAULT)
        }

        fun makeSweepGradient(center: Point, colors: IntArray, positions: FloatArray?): Shader {
            return makeSweepGradient(center.x, center.y, colors, positions)
        }

        fun makeSweepGradient(x: Float, y: Float, colors: IntArray, positions: FloatArray?): Shader {
            return makeSweepGradient(x, y, 0f, 360f, colors, positions, GradientStyle.Companion.DEFAULT)
        }

        fun makeSweepGradient(center: Point, colors: IntArray, positions: FloatArray?, style: GradientStyle): Shader {
            return makeSweepGradient(center.x, center.y, colors, positions, style)
        }

        fun makeSweepGradient(
            x: Float,
            y: Float,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeSweepGradient(x, y, 0f, 360f, colors, positions, style)
        }

        fun makeSweepGradient(
            center: Point,
            startAngle: Float,
            endAngle: Float,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeSweepGradient(center.x, center.y, startAngle, endAngle, colors, positions, style)
        }

        fun makeSweepGradient(
            x: Float,
            y: Float,
            startAngle: Float,
            endAngle: Float,
            colors: IntArray,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            require(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
            Stats.onNativeCall()
            return Shader(
                interopScope {
                    _nMakeSweepGradient(
                        x,
                        y,
                        startAngle,
                        endAngle,
                        toInterop(colors),
                        toInterop(positions),
                        colors.size,
                        style.tileMode.ordinal,
                        style._getFlags(),
                        toInterop(style._getMatrixArray())
                    )
                }
            )
        }

        fun makeSweepGradient(
            center: Point,
            startAngle: Float,
            endAngle: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return makeSweepGradient(center.x, center.y, startAngle, endAngle, colors, cs, positions, style)
        }

        fun makeSweepGradient(
            x: Float,
            y: Float,
            startAngle: Float,
            endAngle: Float,
            colors: Array<Color4f>,
            cs: ColorSpace?,
            positions: FloatArray?,
            style: GradientStyle
        ): Shader {
            return try {
                require(positions == null || colors.size == positions.size) { "colors.length " + colors.size + "!= positions.length " + positions!!.size }
                Stats.onNativeCall()
                Shader(
                    interopScope {
                        _nMakeSweepGradientCS(
                            x,
                            y,
                            startAngle,
                            endAngle,
                            toInterop(Color4f.flattenArray(colors)),
                            getPtr(cs),
                            toInterop(positions),
                            colors.size,
                            style.tileMode.ordinal,
                            style._getFlags(),
                            toInterop(style._getMatrixArray())
                        )
                    }
                )
            } finally {
                reachabilityBarrier(cs)
            }
        }

        //
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

        init {
            staticLoad()
        }
    }

    fun makeWithColorFilter(f: ColorFilter?): Shader {
        return try {
            Shader(_nMakeWithColorFilter(_ptr, getPtr(f)))
        } finally {
            reachabilityBarrier(this)
            reachabilityBarrier(f)
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeEmpty")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Shader__1nMakeEmpty")
private external fun Shader_nMakeEmpty(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeWithColorFilter")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Shader__1nMakeWithColorFilter")
private external fun _nMakeWithColorFilter(ptr: NativePointer, colorFilterPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeLinearGradient")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Shader__1nMakeLinearGradient")
private external fun _nMakeLinearGradient(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    colors: InteropPointer,
    positions: InteropPointer,
    count: Int,
    tileType: Int,
    flags: Int,
    matrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeLinearGradientCS")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Shader__1nMakeLinearGradientCS")
private external fun _nMakeLinearGradientCS(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    colors: InteropPointer,
    colorSpacePtr: NativePointer,
    positions: InteropPointer,
    count: Int,
    tileType: Int,
    flags: Int,
    matrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeRadialGradient")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Shader__1nMakeRadialGradient")
private external fun _nMakeRadialGradient(
    x: Float,
    y: Float,
    r: Float,
    colors: InteropPointer,
    positions: InteropPointer,
    count: Int,
    tileType: Int,
    flags: Int,
    matrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeRadialGradientCS")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Shader__1nMakeRadialGradientCS")
private external fun _nMakeRadialGradientCS(
    x: Float,
    y: Float,
    r: Float,
    colors: InteropPointer,
    colorSpacePtr: NativePointer,
    positions: InteropPointer,
    count: Int,
    tileType: Int,
    flags: Int,
    matrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradient")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradient")
private external fun _nMakeTwoPointConicalGradient(
    x0: Float,
    y0: Float,
    r0: Float,
    x1: Float,
    y1: Float,
    r1: Float,
    colors: InteropPointer,
    positions: InteropPointer,
    count: Int,
    tileType: Int,
    flags: Int,
    matrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradientCS")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Shader__1nMakeTwoPointConicalGradientCS")
private external fun _nMakeTwoPointConicalGradientCS(
    x0: Float,
    y0: Float,
    r0: Float,
    x1: Float,
    y1: Float,
    r1: Float,
    colors: InteropPointer,
    colorSpacePtr: NativePointer,
    positions: InteropPointer,
    count: Int,
    tileType: Int,
    flags: Int,
    matrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeSweepGradient")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Shader__1nMakeSweepGradient")
private external fun _nMakeSweepGradient(
    x: Float,
    y: Float,
    startAngle: Float,
    endAngle: Float,
    colors: InteropPointer,
    positions: InteropPointer,
    count: Int,
    tileType: Int,
    flags: Int,
    matrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeSweepGradientCS")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Shader__1nMakeSweepGradientCS")
private external fun _nMakeSweepGradientCS(
    x: Float,
    y: Float,
    startAngle: Float,
    endAngle: Float,
    colors: InteropPointer,
    colorSpacePtr: NativePointer,
    positions: InteropPointer,
    count: Int,
    tileType: Int,
    flags: Int,
    matrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeFractalNoise")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Shader__1nMakeFractalNoise")
private external fun _nMakeFractalNoise(
    baseFrequencyX: Float,
    baseFrequencyY: Float,
    numOctaves: Int,
    seed: Float,
    tileWidth: Int,
    tileHeight: Int,
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeTurbulence")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Shader__1nMakeTurbulence")
private external fun _nMakeTurbulence(
    baseFrequencyX: Float,
    baseFrequencyY: Float,
    numOctaves: Int,
    seed: Float,
    tileWidth: Int,
    tileHeight: Int,
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeColor")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Shader__1nMakeColor")
private external fun _nMakeColor(color: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeColorCS")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Shader__1nMakeColorCS")
private external fun _nMakeColorCS(r: Float, g: Float, b: Float, a: Float, colorSpacePtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_Shader__1nMakeBlend")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_Shader__1nMakeBlend")
private external fun _nMakeBlend(blendMode: Int, dst: NativePointer, src: NativePointer): NativePointer
