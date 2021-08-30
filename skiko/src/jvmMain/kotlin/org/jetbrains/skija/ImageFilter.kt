package org.jetbrains.skija

import org.jetbrains.skija.impl.Library.Companion.staticLoad
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.skija.impl.RefCnt
import org.jetbrains.skija.impl.Native
import org.jetbrains.skija.impl.Stats
import java.lang.ref.Reference
import java.util.*

class ImageFilter @ApiStatus.Internal constructor(ptr: Long) : RefCnt(ptr) {
    companion object {
        fun makeAlphaThreshold(
            r: Region?,
            innerMin: Float,
            outerMax: Float,
            input: ImageFilter?,
            crop: IRect?
        ): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    _nMakeAlphaThreshold(
                        Native.Companion.getPtr(
                            r
                        ), innerMin, outerMax, Native.Companion.getPtr(input), crop
                    )
                )
            } finally {
                Reference.reachabilityFence(r)
                Reference.reachabilityFence(input)
            }
        }

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
                ImageFilter(
                    _nMakeArithmetic(
                        k1,
                        k2,
                        k3,
                        k4,
                        enforcePMColor,
                        Native.Companion.getPtr(bg),
                        Native.Companion.getPtr(fg),
                        crop
                    )
                )
            } finally {
                Reference.reachabilityFence(bg)
                Reference.reachabilityFence(fg)
            }
        }

        fun makeBlend(blendMode: BlendMode, bg: ImageFilter?, fg: ImageFilter?, crop: IRect?): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    _nMakeBlend(
                        blendMode.ordinal,
                        Native.Companion.getPtr(bg),
                        Native.Companion.getPtr(fg),
                        crop
                    )
                )
            } finally {
                Reference.reachabilityFence(bg)
                Reference.reachabilityFence(fg)
            }
        }

        @JvmOverloads
        fun makeBlur(
            sigmaX: Float,
            sigmaY: Float,
            mode: FilterTileMode,
            input: ImageFilter? = null,
            crop: IRect? = null
        ): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    _nMakeBlur(
                        sigmaX,
                        sigmaY,
                        mode.ordinal,
                        Native.Companion.getPtr(input),
                        crop
                    )
                )
            } finally {
                Reference.reachabilityFence(input)
            }
        }

        fun makeColorFilter(f: ColorFilter?, input: ImageFilter?, crop: IRect?): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    _nMakeColorFilter(
                        Native.Companion.getPtr(
                            f
                        ), Native.Companion.getPtr(input), crop
                    )
                )
            } finally {
                Reference.reachabilityFence(f)
                Reference.reachabilityFence(input)
            }
        }

        fun makeCompose(outer: ImageFilter?, inner: ImageFilter?): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    _nMakeCompose(
                        Native.Companion.getPtr(
                            outer
                        ), Native.Companion.getPtr(inner)
                    )
                )
            } finally {
                Reference.reachabilityFence(outer)
                Reference.reachabilityFence(inner)
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
                ImageFilter(
                    _nMakeDisplacementMap(
                        x.ordinal,
                        y.ordinal,
                        scale,
                        Native.Companion.getPtr(displacement),
                        Native.Companion.getPtr(color),
                        crop
                    )
                )
            } finally {
                Reference.reachabilityFence(displacement)
                Reference.reachabilityFence(color)
            }
        }

        @JvmOverloads
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
                ImageFilter(
                    _nMakeDropShadow(
                        dx,
                        dy,
                        sigmaX,
                        sigmaY,
                        color,
                        Native.Companion.getPtr(input),
                        crop
                    )
                )
            } finally {
                Reference.reachabilityFence(input)
            }
        }

        @JvmOverloads
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
                ImageFilter(
                    _nMakeDropShadowOnly(
                        dx,
                        dy,
                        sigmaX,
                        sigmaY,
                        color,
                        Native.Companion.getPtr(input),
                        crop
                    )
                )
            } finally {
                Reference.reachabilityFence(input)
            }
        }

        fun makeImage(image: Image): ImageFilter {
            val r: Rect = Rect.Companion.makeWH(image.width.toFloat(), image.height.toFloat())
            return makeImage(image, r, r, SamplingMode.Companion.DEFAULT)
        }

        fun makeImage(image: Image?, src: Rect, dst: Rect, mode: SamplingMode): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    _nMakeImage(
                        Native.Companion.getPtr(
                            image
                        ),
                        src.left,
                        src.top,
                        src.right,
                        src.bottom,
                        dst.left,
                        dst.top,
                        dst.right,
                        dst.bottom,
                        mode._pack()
                    )
                )
            } finally {
                Reference.reachabilityFence(image)
            }
        }

        fun makeMagnifier(r: Rect, inset: Float, input: ImageFilter?, crop: IRect?): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    _nMakeMagnifier(
                        r.left,
                        r.top,
                        r.right,
                        r.bottom,
                        inset,
                        Native.Companion.getPtr(input),
                        crop
                    )
                )
            } finally {
                Reference.reachabilityFence(input)
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
                    _nMakeMatrixConvolution(
                        kernelW,
                        kernelH,
                        kernel,
                        gain,
                        bias,
                        offsetX,
                        offsetY,
                        tileMode.ordinal,
                        convolveAlpha,
                        Native.getPtr(input),
                        crop
                    )
                )
            } finally {
                Reference.reachabilityFence(input)
            }
        }

        fun makeMatrixTransform(matrix: Matrix33, mode: SamplingMode, input: ImageFilter?): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    _nMakeMatrixTransform(
                        matrix.mat,
                        mode._pack(),
                        Native.Companion.getPtr(input)
                    )
                )
            } finally {
                Reference.reachabilityFence(input)
            }
        }

        fun makeMerge(filters: Array<ImageFilter?>, crop: IRect?): ImageFilter {
            return try {
                Stats.onNativeCall()
                val filterPtrs = LongArray(filters.size)
                Arrays.setAll(filterPtrs) { i: Int -> Native.Companion.getPtr(filters[i]) }
                ImageFilter(_nMakeMerge(filterPtrs, crop))
            } finally {
                Reference.reachabilityFence(filters)
            }
        }

        fun makeOffset(dx: Float, dy: Float, input: ImageFilter?, crop: IRect?): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    _nMakeOffset(
                        dx,
                        dy,
                        Native.Companion.getPtr(input),
                        crop
                    )
                )
            } finally {
                Reference.reachabilityFence(input)
            }
        }

        fun makePaint(paint: Paint?, crop: IRect?): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    _nMakePaint(
                        Native.Companion.getPtr(
                            paint
                        ), crop
                    )
                )
            } finally {
                Reference.reachabilityFence(paint)
            }
        }

        // public static ImageFilter makePicture(Picture picture, Rect target) {
        //     Native.onNativeCall();
        //     return new ImageFilter(_nMakePicture(Native.pointer(picture), target.left, target.top, target.right, target.bottom));
        // }
        fun makeTile(src: Rect, dst: Rect, input: ImageFilter?): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    _nMakeTile(
                        src.left,
                        src.top,
                        src.right,
                        src.bottom,
                        dst.left,
                        dst.top,
                        dst.right,
                        dst.bottom,
                        Native.Companion.getPtr(input)
                    )
                )
            } finally {
                Reference.reachabilityFence(input)
            }
        }

        fun makeDilate(rx: Float, ry: Float, input: ImageFilter?, crop: IRect?): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    _nMakeDilate(
                        rx,
                        ry,
                        Native.Companion.getPtr(input),
                        crop
                    )
                )
            } finally {
                Reference.reachabilityFence(input)
            }
        }

        fun makeErode(rx: Float, ry: Float, input: ImageFilter?, crop: IRect?): ImageFilter {
            return try {
                Stats.onNativeCall()
                ImageFilter(
                    _nMakeErode(
                        rx,
                        ry,
                        Native.Companion.getPtr(input),
                        crop
                    )
                )
            } finally {
                Reference.reachabilityFence(input)
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
                ImageFilter(
                    _nMakeDistantLitDiffuse(
                        x,
                        y,
                        z,
                        lightColor,
                        surfaceScale,
                        kd,
                        Native.Companion.getPtr(input),
                        crop
                    )
                )
            } finally {
                Reference.reachabilityFence(input)
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
                ImageFilter(
                    _nMakePointLitDiffuse(
                        x,
                        y,
                        z,
                        lightColor,
                        surfaceScale,
                        kd,
                        Native.Companion.getPtr(input),
                        crop
                    )
                )
            } finally {
                Reference.reachabilityFence(input)
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
                ImageFilter(
                    _nMakeSpotLitDiffuse(
                        x0,
                        y0,
                        z0,
                        x1,
                        y1,
                        z1,
                        falloffExponent,
                        cutoffAngle,
                        lightColor,
                        surfaceScale,
                        kd,
                        Native.Companion.getPtr(input),
                        crop
                    )
                )
            } finally {
                Reference.reachabilityFence(input)
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
                ImageFilter(
                    _nMakeDistantLitSpecular(
                        x,
                        y,
                        z,
                        lightColor,
                        surfaceScale,
                        ks,
                        shininess,
                        Native.Companion.getPtr(input),
                        crop
                    )
                )
            } finally {
                Reference.reachabilityFence(input)
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
                ImageFilter(
                    _nMakePointLitSpecular(
                        x,
                        y,
                        z,
                        lightColor,
                        surfaceScale,
                        ks,
                        shininess,
                        Native.Companion.getPtr(input),
                        crop
                    )
                )
            } finally {
                Reference.reachabilityFence(input)
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
                ImageFilter(
                    _nMakeSpotLitSpecular(
                        x0,
                        y0,
                        z0,
                        x1,
                        y1,
                        z1,
                        falloffExponent,
                        cutoffAngle,
                        lightColor,
                        surfaceScale,
                        ks,
                        shininess,
                        Native.Companion.getPtr(input),
                        crop
                    )
                )
            } finally {
                Reference.reachabilityFence(input)
            }
        }

        external fun _nMakeAlphaThreshold(
            regionPtr: Long,
            innerMin: Float,
            outerMax: Float,
            input: Long,
            crop: IRect?
        ): Long

        external fun _nMakeArithmetic(
            k1: Float,
            k2: Float,
            k3: Float,
            k4: Float,
            enforcePMColor: Boolean,
            bg: Long,
            fg: Long,
            crop: IRect?
        ): Long

        external fun _nMakeBlend(blendMode: Int, bg: Long, fg: Long, crop: IRect?): Long
        external fun _nMakeBlur(sigmaX: Float, sigmaY: Float, tileMode: Int, input: Long, crop: IRect?): Long
        external fun _nMakeColorFilter(colorFilterPtr: Long, input: Long, crop: IRect?): Long
        external fun _nMakeCompose(outer: Long, inner: Long): Long
        external fun _nMakeDisplacementMap(
            xChan: Int,
            yChan: Int,
            scale: Float,
            displacement: Long,
            color: Long,
            crop: IRect?
        ): Long

        external fun _nMakeDropShadow(
            dx: Float,
            dy: Float,
            sigmaX: Float,
            sigmaY: Float,
            color: Int,
            input: Long,
            crop: IRect?
        ): Long

        external fun _nMakeDropShadowOnly(
            dx: Float,
            dy: Float,
            sigmaX: Float,
            sigmaY: Float,
            color: Int,
            input: Long,
            crop: IRect?
        ): Long

        external fun _nMakeImage(
            image: Long,
            l0: Float,
            t0: Float,
            r0: Float,
            b0: Float,
            l1: Float,
            t1: Float,
            r1: Float,
            b1: Float,
            samplingMode: Long
        ): Long

        external fun _nMakeMagnifier(
            l: Float,
            t: Float,
            r: Float,
            b: Float,
            inset: Float,
            input: Long,
            crop: IRect?
        ): Long

        external fun _nMakeMatrixConvolution(
            kernelW: Int,
            kernelH: Int,
            kernel: FloatArray?,
            gain: Float,
            bias: Float,
            offsetX: Int,
            offsetY: Int,
            tileMode: Int,
            convolveAlpha: Boolean,
            input: Long,
            crop: IRect?
        ): Long

        external fun _nMakeMatrixTransform(matrix: FloatArray?, samplingMode: Long, input: Long): Long
        external fun _nMakeMerge(filters: LongArray?, crop: IRect?): Long
        external fun _nMakeOffset(dx: Float, dy: Float, input: Long, crop: IRect?): Long
        external fun _nMakePaint(paint: Long, crop: IRect?): Long
        external fun _nMakePicture(picture: Long, l: Float, t: Float, r: Float, b: Float): Long
        external fun _nMakeTile(
            l0: Float,
            t0: Float,
            r0: Float,
            b0: Float,
            l1: Float,
            t1: Float,
            r1: Float,
            b1: Float,
            input: Long
        ): Long

        external fun _nMakeDilate(rx: Float, ry: Float, input: Long, crop: IRect?): Long
        external fun _nMakeErode(rx: Float, ry: Float, input: Long, crop: IRect?): Long
        external fun _nMakeDistantLitDiffuse(
            x: Float,
            y: Float,
            z: Float,
            lightColor: Int,
            surfaceScale: Float,
            kd: Float,
            input: Long,
            crop: IRect?
        ): Long

        external fun _nMakePointLitDiffuse(
            x: Float,
            y: Float,
            z: Float,
            lightColor: Int,
            surfaceScale: Float,
            kd: Float,
            input: Long,
            crop: IRect?
        ): Long

        external fun _nMakeSpotLitDiffuse(
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
            input: Long,
            crop: IRect?
        ): Long

        external fun _nMakeDistantLitSpecular(
            x: Float,
            y: Float,
            z: Float,
            lightColor: Int,
            surfaceScale: Float,
            ks: Float,
            shininess: Float,
            input: Long,
            crop: IRect?
        ): Long

        external fun _nMakePointLitSpecular(
            x: Float,
            y: Float,
            z: Float,
            lightColor: Int,
            surfaceScale: Float,
            ks: Float,
            shininess: Float,
            input: Long,
            crop: IRect?
        ): Long

        external fun _nMakeSpotLitSpecular(
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
            input: Long,
            crop: IRect?
        ): Long

        init {
            staticLoad()
        }
    }
}