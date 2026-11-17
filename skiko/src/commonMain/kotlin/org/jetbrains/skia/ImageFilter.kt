package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.*

/**
 *  Base class for image filters. If one is installed in the paint, then all drawing occurs as
 *  usual, but it is as if the drawing happened into an offscreen (before the xfermode is applied).
 *  This offscreen bitmap will then be handed to the imagefilter, who in turn creates a new bitmap
 *  which is what will finally be drawn to the device (using the original xfermode).
 *
 *  The local space of image filters matches the local space of the drawn geometry. For instance if
 *  there is rotation on the canvas, the blur will be computed along those rotated axes and not in
 *  the device space. In order to achieve this result, the actual drawing of the geometry may happen
 *  in an unrotated coordinate system so that the filtered image can be computed more easily, and
 *  then it will be post transformed to match what would have been produced if the geometry were
 *  drawn with the total canvas matrix to begin with.
 */
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

        /** Returns an image filter that draws [picture] into [target], replacing the filter input. */
        fun makePicture(picture: Picture?, target: Rect): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    _nMakePicture(
                        getPtr(picture),
                        target.left,
                        target.top,
                        target.right,
                        target.bottom
                    )
                )
            } finally {
                reachabilityBarrier(picture)
            }
        }

        /** Returns an image filter that draws [picture] over its cull rect, replacing the filter input. */
        fun makePicture(picture: Picture): ImageFilter = makePicture(picture, picture.cullRect)

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
// Needs ForceNativeThreadState: compiles a shader program the first time it is used, and other callers wait while that
// one-time setup runs.
// Native call path: SkBlenders::Arithmetic -> GetKnownRuntimeEffect
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
private external fun _nMakeBlend(blendMode: Int, bg: NativePointer, fg: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeBlur")
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
private external fun _nMakeBlur(sigmaX: Float, sigmaY: Float, tileMode: Int, input: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeColorFilter")
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
private external fun _nMakeColorFilter(colorFilterPtr: NativePointer, input: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeCompose")
// No ForceNativeThreadState: allocates a filter that holds references to its two inputs, so it is short and cannot
// block.
// Native call path: SkImageFilters::Compose
private external fun _nMakeCompose(outer: NativePointer, inner: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeDisplacementMap")
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
private external fun _nMakeDisplacementMap(
    xChan: Int,
    yChan: Int,
    scale: Float,
    displacement: NativePointer,
    color: NativePointer,
    crop: InteropPointer
): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeDropShadow")
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
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
// No ForceNativeThreadState: allocates a filter that holds a reference to the image and two rectangles without reading
// the image, so it is short and cannot block.
// Native call path: SkImageFilters::Image
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
// No ForceNativeThreadState: allocates a magnifier filter and an optional crop, so it is short and cannot block.
// Native call path: SkImageFilters::Magnifier
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
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
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
// No ForceNativeThreadState: checks that a matrix can be inverted and allocates a matrix transform filter, so it is
// short and cannot block.
// Native call path: SkImageFilters::MatrixTransform
private external fun _nMakeMatrixTransform(matrix: InteropPointer, samplingModeVal1: Int, samplingModeVal2: Int, input: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeMerge")
// Needs ForceNativeThreadState: copies an array of filters of any length into the filter, so its time grows with the
// number of filters.
// Native call path: SkImageFilters::Merge -> SkImageFilter_Base
@ForceNativeThreadState
private external fun _nMakeMerge(filters: InteropPointer, filtersLength: Int, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeOffset")
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
private external fun _nMakeOffset(dx: Float, dy: Float, input: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeShader")
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
private external fun _nMakeShader(shader: NativePointer, dither: Boolean, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakePicture")
// No ForceNativeThreadState: reads the bounds of a picture and allocates a filter that holds a reference to it, so it
// is short and cannot block.
// Native call path: SkImageFilters::Picture
private external fun _nMakePicture(picture: NativePointer, l: Float, t: Float, r: Float, b: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeRuntimeShader")
// Needs ForceNativeThreadState: searches the shader's child inputs one by one and copies them, so its time grows with
// the number of children.
// Native call path: SkImageFilters::RuntimeShader -> SkRuntimeEffect::findChild
@ForceNativeThreadState
private external fun _nMakeRuntimeShader(runtimeShaderBuilderPtr: NativePointer, childShaderName: InteropPointer, input: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeRuntimeShaderFromArray")
// Needs ForceNativeThreadState: copies N input filters and their names, then checks each name against the others, so
// its time grows with N squared, where N is the number of inputs.
// Native call path: SkImageFilters::RuntimeShader
@ForceNativeThreadState
private external fun _nMakeRuntimeShaderFromArray(runtimeShaderBuilderPtr: NativePointer, childShaderNames: InteropPointer, inputs: InteropPointer, inputLength: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeTile")
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
private external fun _nMakeDilate(rx: Float, ry: Float, input: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeErode")
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
private external fun _nMakeErode(rx: Float, ry: Float, input: NativePointer, crop: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ImageFilter__1nMakeDistantLitDiffuse")
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
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
// Needs ForceNativeThreadState: rejecting an inverted crop can free a filter and wait on a cache lock held while
// another thread unmaps a file.
// Native call path: SkImageFilters::Crop -> ~SkImageFilter_Base -> purgeByImageFilter
@ForceNativeThreadState
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
