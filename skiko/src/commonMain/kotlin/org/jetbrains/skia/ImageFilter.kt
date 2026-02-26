package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.*

class ImageFilter internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {

        fun makeArithmetic(
            k1: Float,
            k2: Float,
            k3: Float,
            k4: Float,
            enforcePMColor: Boolean,
            bg: ImageFilter?,
            fg: ImageFilter?,
            crop: IRect?
        ): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakeArithmetic(
                            k1,
                            k2,
                            k3,
                            k4,
                            enforcePMColor,
                            getPtr(bg),
                            getPtr(fg),
                            toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(bg)
                reachabilityBarrier(fg)
            }
        }

        fun makeBlend(blendMode: BlendMode, bg: ImageFilter?, fg: ImageFilter?, crop: IRect?): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakeBlend(
                            blendMode.ordinal,
                            getPtr(bg),
                            getPtr(fg),
                            toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(bg)
                reachabilityBarrier(fg)
            }
        }

        fun makeBlur(
            sigmaX: Float,
            sigmaY: Float,
            mode: FilterTileMode,
            input: ImageFilter? = null,
            crop: IRect? = null
        ): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakeBlur(
                            sigmaX,
                            sigmaY,
                            mode.ordinal,
                            getPtr(input),
                            toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(input)
            }
        }

        fun makeColorFilter(f: ColorFilter?, input: ImageFilter?, crop: IRect?): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakeColorFilter(
                            getPtr(f), getPtr(input),
                            toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(f)
                reachabilityBarrier(input)
            }
        }

        fun makeCompose(outer: ImageFilter?, inner: ImageFilter?): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    _nMakeCompose(
                        getPtr(outer), getPtr(inner)
                    )
                )
            } finally {
                reachabilityBarrier(outer)
                reachabilityBarrier(inner)
            }
        }

        fun makeDisplacementMap(
            x: ColorChannel,
            y: ColorChannel,
            scale: Float,
            displacement: ImageFilter?,
            color: ImageFilter?,
            crop: IRect?
        ): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakeDisplacementMap(
                            x.ordinal,
                            y.ordinal,
                            scale,
                            getPtr(displacement),
                            getPtr(color),
                            toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(displacement)
                reachabilityBarrier(color)
            }
        }

        fun makeDropShadow(
            dx: Float,
            dy: Float,
            sigmaX: Float,
            sigmaY: Float,
            color: Int,
            input: ImageFilter? = null,
            crop: IRect? = null
        ): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakeDropShadow(
                            dx,
                            dy,
                            sigmaX,
                            sigmaY,
                            color,
                            getPtr(input),
                            toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(input)
            }
        }

        fun makeDropShadowOnly(
            dx: Float,
            dy: Float,
            sigmaX: Float,
            sigmaY: Float,
            color: Int,
            input: ImageFilter? = null,
            crop: IRect? = null
        ): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakeDropShadowOnly(
                            dx,
                            dy,
                            sigmaX,
                            sigmaY,
                            color,
                            getPtr(input),
                            toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(input)
            }
        }

        fun makeImage(image: Image): ImageFilter {
            return makeImage(
                image,
                0f,
                0f,
                image.width.toFloat(),
                image.height.toFloat(),
                0f,
                0f,
                image.width.toFloat(),
                image.height.toFloat(),
                SamplingMode.DEFAULT
            )
        }

        fun makeImage(image: Image?, src: Rect, dst: Rect, mode: SamplingMode): ImageFilter {
            return makeImage(
                image,
                src.left,
                src.top,
                src.right,
                src.bottom,
                dst.left,
                dst.top,
                dst.right,
                dst.bottom,
                mode
            )
        }

        fun makeImage(
            image: Image?,
            srcLeft: Float,
            srcTop: Float,
            srcRight: Float,
            srcBottom: Float,
            dstLeft: Float,
            dstTop: Float,
            dstRight: Float,
            dstBottom: Float,
            mode: SamplingMode
        ): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    _nMakeImage(
                        getPtr(image),
                        srcLeft,
                        srcTop,
                        srcRight,
                        srcBottom,
                        dstLeft,
                        dstTop,
                        dstRight,
                        dstBottom,
                        mode._packedInt1(),
                        mode._packedInt2()
                    )
                )
            } finally {
                reachabilityBarrier(image)
            }
        }

        fun makeMagnifier(r: Rect, zoomAmount: Float, inset: Float, samplingMode: SamplingMode, input: ImageFilter?, crop: IRect?): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakeMagnifier(
                            r.left,
                            r.top,
                            r.right,
                            r.bottom,
                            zoomAmount,
                            inset,
                            samplingMode._packedInt1(),
                            samplingMode._packedInt2(),
                            getPtr(input),
                            toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(input)
            }
        }

        fun makeMatrixConvolution(
            kernelW: Int,
            kernelH: Int,
            kernel: FloatArray?,
            gain: Float,
            bias: Float,
            offsetX: Int,
            offsetY: Int,
            tileMode: FilterTileMode,
            convolveAlpha: Boolean,
            input: ImageFilter?,
            crop: IRect?
        ): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    interopScope {
                        _nMakeMatrixConvolution(
                            kernelW,
                            kernelH,
                            toInterop(kernel),
                            gain,
                            bias,
                            offsetX,
                            offsetY,
                            tileMode.ordinal,
                            convolveAlpha,
                            getPtr(input),
                            toInterop(crop?.serializeToIntArray())
                        )
                    }
                )
            } finally {
                reachabilityBarrier(input)
            }
        }

        fun makeMatrixTransform(matrix: Matrix33, mode: SamplingMode, input: ImageFilter?): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    interopScope {
                        _nMakeMatrixTransform(
                            toInterop(matrix.mat),
                            mode._packedInt1(),
                            mode._packedInt2(),
                            getPtr(input)
                        )
                    }
                )
            } finally {
                reachabilityBarrier(input)
            }
        }

        fun makeMerge(filters: Array<ImageFilter?>, crop: IRect?): ImageFilter {
            return interopScope {
                try {
                    Stats.onNativeCall()
                    val filterPtrs = NativePointerArray(filters.size)
                    for (i in filters.indices) filterPtrs[i] = getPtr(filters[i])
                    ImageFilter(
                        _nMakeMerge(
                            filters = toInterop(filterPtrs),
                            filtersLength = filters.size,
                            crop = toInterop(crop?.serializeToIntArray())
                        )
                    )
                } finally {
                    reachabilityBarrier(filters)
                }
            }
        }

        fun makeOffset(dx: Float, dy: Float, input: ImageFilter?, crop: IRect?): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakeOffset(
                            dx, dy,
                            getPtr(input),
                            toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(input)
            }
        }

        fun makeShader(shader: Shader, dither: Boolean = false, crop: IRect?): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakeShader(
                            shader = getPtr(shader),
                            dither = dither,
                            crop = toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(shader)
            }
        }

        fun makeRuntimeShader(runtimeShaderBuilder: RuntimeShaderBuilder, shaderName: String, input: ImageFilter?): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakeRuntimeShader(
                            runtimeShaderBuilderPtr = getPtr(runtimeShaderBuilder),
                            childShaderName = toInterop(shaderName),
                            input = getPtr(input)
                        )
                    )
                }
            } finally {
                reachabilityBarrier(runtimeShaderBuilder)
                reachabilityBarrier(input)
            }
        }


        fun makeRuntimeShader(runtimeShaderBuilder: RuntimeShaderBuilder, shaderNames: Array<String>, inputs: Array<ImageFilter?>): ImageFilter {
            return try {
                Stats.onNativeCall()
                require(shaderNames.size == inputs.size)
                interopScope {
                    val inputPtrs = NativePointerArray(inputs.size)
                    for (i in inputs.indices) inputPtrs[i] = getPtr(inputs[i])
                    ImageFilter(
                        _nMakeRuntimeShaderFromArray(
                            runtimeShaderBuilderPtr = getPtr(runtimeShaderBuilder),
                            childShaderNames = toInterop(shaderNames),
                            inputs = toInterop(inputPtrs),
                            inputLength = inputPtrs.size
                        )
                    )
                }
            } finally {
                reachabilityBarrier(runtimeShaderBuilder)
                reachabilityBarrier(inputs)
            }
        }

        fun makeTile(src: Rect, dst: Rect, input: ImageFilter?): ImageFilter {
            return makeTile(
                src.left,
                src.top,
                src.right,
                src.bottom,
                dst.left,
                dst.top,
                dst.right,
                dst.bottom,
                input
            )
        }

        fun makeTile(
            srcLeft: Float,
            srcTop: Float,
            srcRight: Float,
            srcBottom: Float,
            dstLeft: Float,
            dstTop: Float,
            dstRight: Float,
            dstBottom: Float,
            input: ImageFilter?
        ): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    _nMakeTile(
                        srcLeft,
                        srcTop,
                        srcRight,
                        srcBottom,
                        dstLeft,
                        dstTop,
                        dstRight,
                        dstBottom,
                        getPtr(input)
                    )
                )
            } finally {
                reachabilityBarrier(input)
            }
        }

        fun makeDilate(rx: Float, ry: Float, input: ImageFilter?, crop: IRect?): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakeDilate(
                            rx,
                            ry,
                            getPtr(input),
                            toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(input)
            }
        }

        fun makeErode(rx: Float, ry: Float, input: ImageFilter?, crop: IRect?): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakeErode(
                            rx, ry,
                            getPtr(input),
                            toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(input)
            }
        }

        fun makeDistantLitDiffuse(
            x: Float,
            y: Float,
            z: Float,
            lightColor: Int,
            surfaceScale: Float,
            kd: Float,
            input: ImageFilter?,
            crop: IRect?
        ): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakeDistantLitDiffuse(
                            x, y, z,
                            lightColor,
                            surfaceScale,
                            kd,
                            getPtr(input),
                            toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(input)
            }
        }

        fun makePointLitDiffuse(
            x: Float,
            y: Float,
            z: Float,
            lightColor: Int,
            surfaceScale: Float,
            kd: Float,
            input: ImageFilter?,
            crop: IRect?
        ): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakePointLitDiffuse(
                            x, y, z,
                            lightColor,
                            surfaceScale,
                            kd,
                            getPtr(input),
                            toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(input)
            }
        }

        fun makeSpotLitDiffuse(
            x0: Float,
            y0: Float,
            z0: Float,
            x1: Float,
            y1: Float,
            z1: Float,
            falloffExponent: Float,
            cutoffAngle: Float,
            lightColor: Int,
            surfaceScale: Float,
            kd: Float,
            input: ImageFilter?,
            crop: IRect?
        ): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakeSpotLitDiffuse(
                            x0, y0, z0,
                            x1, y1, z1,
                            falloffExponent,
                            cutoffAngle,
                            lightColor,
                            surfaceScale,
                            kd,
                            getPtr(input),
                            toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(input)
            }
        }

        fun makeDistantLitSpecular(
            x: Float,
            y: Float,
            z: Float,
            lightColor: Int,
            surfaceScale: Float,
            ks: Float,
            shininess: Float,
            input: ImageFilter?,
            crop: IRect?
        ): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakeDistantLitSpecular(
                            x,
                            y,
                            z,
                            lightColor,
                            surfaceScale,
                            ks,
                            shininess,
                            getPtr(input),
                            toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(input)
            }
        }

        fun makePointLitSpecular(
            x: Float,
            y: Float,
            z: Float,
            lightColor: Int,
            surfaceScale: Float,
            ks: Float,
            shininess: Float,
            input: ImageFilter?,
            crop: IRect?
        ): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakePointLitSpecular(
                            x, y, z,
                            lightColor,
                            surfaceScale,
                            ks,
                            shininess,
                            getPtr(input),
                            toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(input)
            }
        }

        fun makeSpotLitSpecular(
            x0: Float,
            y0: Float,
            z0: Float,
            x1: Float,
            y1: Float,
            z1: Float,
            falloffExponent: Float,
            cutoffAngle: Float,
            lightColor: Int,
            surfaceScale: Float,
            ks: Float,
            shininess: Float,
            input: ImageFilter?,
            crop: IRect?
        ): ImageFilter {
            return try {
                Stats.onNativeCall()
                interopScope {
                    ImageFilter(
                        _nMakeSpotLitSpecular(
                            x0, y0, z0,
                            x1, y1, z1,
                            falloffExponent,
                            cutoffAngle,
                            lightColor,
                            surfaceScale,
                            ks,
                            shininess,
                            getPtr(input),
                            toInterop(crop?.serializeToIntArray())
                        )
                    )
                }
            } finally {
                reachabilityBarrier(input)
            }
        }

        init {
            staticLoad()
        }
    }
}

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeArithmetic")
private external fun _nMakeArithmetic(
    k1: Float,
    k2: Float,
    k3: Float,
    k4: Float,
    enforcePMColor: Boolean,
    bg: NativePointer,
    fg: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeBlend")
private external fun _nMakeBlend(blendMode: Int, bg: NativePointer, fg: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeBlur")
private external fun _nMakeBlur(sigmaX: Float, sigmaY: Float, tileMode: Int, input: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeColorFilter")
private external fun _nMakeColorFilter(colorFilterPtr: NativePointer, input: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeCompose")
private external fun _nMakeCompose(outer: NativePointer, inner: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeDisplacementMap")
private external fun _nMakeDisplacementMap(
    xChan: Int,
    yChan: Int,
    scale: Float,
    displacement: NativePointer,
    color: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeDropShadow")
private external fun _nMakeDropShadow(
    dx: Float,
    dy: Float,
    sigmaX: Float,
    sigmaY: Float,
    color: Int,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeDropShadowOnly")
private external fun _nMakeDropShadowOnly(
    dx: Float,
    dy: Float,
    sigmaX: Float,
    sigmaY: Float,
    color: Int,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeImage")
private external fun _nMakeImage(
    image: NativePointer,
    l0: Float,
    t0: Float,
    r0: Float,
    b0: Float,
    l1: Float,
    t1: Float,
    r1: Float,
    b1: Float,
    samplingModeVal1: Int,
    samplingModeVal2: Int,
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeMagnifier")
private external fun _nMakeMagnifier(
    l: Float,
    t: Float,
    r: Float,
    b: Float,
    zoomAmount: Float,
    inset: Float,
    samplingModeVal1: Int,
    samplingModeVal2: Int,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeMatrixConvolution")
private external fun _nMakeMatrixConvolution(
    kernelW: Int,
    kernelH: Int,
    kernel: InteropPointer,
    gain: Float,
    bias: Float,
    offsetX: Int,
    offsetY: Int,
    tileMode: Int,
    convolveAlpha: Boolean,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeMatrixTransform")
private external fun _nMakeMatrixTransform(matrix: InteropPointer, samplingModeVal1: Int, samplingModeVal2: Int, input: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeMerge")
private external fun _nMakeMerge(filters: InteropPointer, filtersLength: Int, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeOffset")
private external fun _nMakeOffset(dx: Float, dy: Float, input: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeShader")
private external fun _nMakeShader(shader: NativePointer, dither: Boolean, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakePicture")
private external fun _nMakePicture(picture: NativePointer, l: Float, t: Float, r: Float, b: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeRuntimeShader")
private external fun _nMakeRuntimeShader(runtimeShaderBuilderPtr: NativePointer, childShaderName: InteropPointer, input: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeRuntimeShaderFromArray")
private external fun _nMakeRuntimeShaderFromArray(runtimeShaderBuilderPtr: NativePointer, childShaderNames: InteropPointer, inputs: InteropPointer, inputLength: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeTile")
private external fun _nMakeTile(
    l0: Float,
    t0: Float,
    r0: Float,
    b0: Float,
    l1: Float,
    t1: Float,
    r1: Float,
    b1: Float,
    input: NativePointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeDilate")
private external fun _nMakeDilate(rx: Float, ry: Float, input: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeErode")
private external fun _nMakeErode(rx: Float, ry: Float, input: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeDistantLitDiffuse")
private external fun _nMakeDistantLitDiffuse(
    x: Float,
    y: Float,
    z: Float,
    lightColor: Int,
    surfaceScale: Float,
    kd: Float,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakePointLitDiffuse")
private external fun _nMakePointLitDiffuse(
    x: Float,
    y: Float,
    z: Float,
    lightColor: Int,
    surfaceScale: Float,
    kd: Float,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeSpotLitDiffuse")
private external fun _nMakeSpotLitDiffuse(
    x0: Float,
    y0: Float,
    z0: Float,
    x1: Float,
    y1: Float,
    z1: Float,
    falloffExponent: Float,
    cutoffAngle: Float,
    lightColor: Int,
    surfaceScale: Float,
    kd: Float,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeDistantLitSpecular")
private external fun _nMakeDistantLitSpecular(
    x: Float,
    y: Float,
    z: Float,
    lightColor: Int,
    surfaceScale: Float,
    ks: Float,
    shininess: Float,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakePointLitSpecular")
private external fun _nMakePointLitSpecular(
    x: Float,
    y: Float,
    z: Float,
    lightColor: Int,
    surfaceScale: Float,
    ks: Float,
    shininess: Float,
    input: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeSpotLitSpecular")
private external fun _nMakeSpotLitSpecular(
    x0: Float,
    y0: Float,
    z0: Float,
    x1: Float,
    y1: Float,
    z1: Float,
    falloffExponent: Float,
    cutoffAngle: Float,
    lightColor: Int,
    surfaceScale: Float,
    ks: Float,
    shininess: Float,
    input: NativePointer,
    crop: InteropPointer
): NativePointer
