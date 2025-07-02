package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.NativePointerArray
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier

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
                        ImageFilter_nMakeArithmetic(
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
                        ImageFilter_nMakeBlend(
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
                        ImageFilter_nMakeBlur(
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
                        ImageFilter_nMakeColorFilter(
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
                    ImageFilter_nMakeCompose(
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
                        ImageFilter_nMakeDisplacementMap(
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
                        ImageFilter_nMakeDropShadow(
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
                        ImageFilter_nMakeDropShadowOnly(
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
            val r: Rect = Rect.makeWH(image.width.toFloat(), image.height.toFloat())
            return makeImage(image, r, r, SamplingMode.DEFAULT)
        }

        fun makeImage(image: Image?, src: Rect, dst: Rect, mode: SamplingMode): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    ImageFilter_nMakeImage(
                        getPtr(image),
                        src.left,
                        src.top,
                        src.right,
                        src.bottom,
                        dst.left,
                        dst.top,
                        dst.right,
                        dst.bottom,
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
                        ImageFilter_nMakeMagnifier(
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
                        ImageFilter_nMakeMatrixConvolution(
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
                        ImageFilter_nMakeMatrixTransform(
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
                        ImageFilter_nMakeMerge(
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
                        ImageFilter_nMakeOffset(
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
                        ImageFilter_nMakeShader(
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
                        ImageFilter_nMakeRuntimeShader(
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
                        ImageFilter_nMakeRuntimeShaderFromArray(
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
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    ImageFilter_nMakeTile(
                        src.left,
                        src.top,
                        src.right,
                        src.bottom,
                        dst.left,
                        dst.top,
                        dst.right,
                        dst.bottom,
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
                        ImageFilter_nMakeDilate(
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
                        ImageFilter_nMakeErode(
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
                        ImageFilter_nMakeDistantLitDiffuse(
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
                        ImageFilter_nMakePointLitDiffuse(
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
                        ImageFilter_nMakeSpotLitDiffuse(
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
                        ImageFilter_nMakeDistantLitSpecular(
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
                        ImageFilter_nMakePointLitSpecular(
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
                        ImageFilter_nMakeSpotLitSpecular(
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