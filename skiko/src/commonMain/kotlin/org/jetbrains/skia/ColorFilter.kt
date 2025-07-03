package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.getPtr
import org.jetbrains.skia.impl.interopScope
import org.jetbrains.skia.impl.reachabilityBarrier

class ColorFilter : RefCnt {
    companion object {
        fun makeComposed(outer: ColorFilter?, inner: ColorFilter?): ColorFilter {
            return try {
                Stats.onNativeCall()
                ColorFilter(
                    ColorFilter_nMakeComposed(
                        getPtr(outer),
                        getPtr(inner)
                    )
                )
            } finally {
                reachabilityBarrier(outer)
                reachabilityBarrier(inner)
            }
        }

        fun makeBlend(color: Int, mode: BlendMode): ColorFilter {
            Stats.onNativeCall()
            return ColorFilter(ColorFilter_nMakeBlend(color, mode.ordinal))
        }

        fun makeMatrix(matrix: ColorMatrix): ColorFilter {
            Stats.onNativeCall()
            return ColorFilter(
                interopScope {
                    ColorFilter_nMakeMatrix(toInterop(matrix.mat))
                }
            )
        }

        fun makeHSLAMatrix(matrix: ColorMatrix): ColorFilter {
            Stats.onNativeCall()
            return ColorFilter(
                interopScope {
                    ColorFilter_nMakeHSLAMatrix(toInterop(matrix.mat))
                }
            )
        }

        fun makeLerp(dst: ColorFilter?, src: ColorFilter?, t: Float): ColorFilter {
            return try {
                ColorFilter(
                    ColorFilter_nMakeLerp(
                        t,
                        getPtr(dst),
                        getPtr(src)
                    )
                )
            } finally {
                reachabilityBarrier(dst)
                reachabilityBarrier(src)
            }
        }

        fun makeLighting(colorMul: Int, colorAdd: Int): ColorFilter {
            return ColorFilter(ColorFilter_nMakeLighting(colorMul, colorAdd))
        }

        fun makeHighContrast(grayscale: Boolean, mode: InversionMode, contrast: Float): ColorFilter {
            return ColorFilter(ColorFilter_nMakeHighContrast(grayscale, mode.ordinal, contrast))
        }

        fun makeTable(table: ByteArray): ColorFilter {
            require(table.size == 256) { "Expected 256 elements, got " + table.size }
            return ColorFilter(
                interopScope {
                    ColorFilter_nMakeTable(toInterop(table))
                }
            )
        }

        fun makeTableARGB(a: ByteArray?, r: ByteArray?, g: ByteArray?, b: ByteArray?): ColorFilter {
            require(a == null || a.size == 256) { "Expected 256 elements in a[], got " + a!!.size }
            require(r == null || r.size == 256) { "Expected 256 elements in r[], got " + r!!.size }
            require(g == null || g.size == 256) { "Expected 256 elements in g[], got " + g!!.size }
            require(b == null || b.size == 256) { "Expected 256 elements in b[], got " + b!!.size }
            interopScope {
                return ColorFilter(
                    ColorFilter_nMakeTableARGB(
                        toInterop(a),
                        toInterop(r),
                        toInterop(g),
                        toInterop(b),
                    )
                )
            }
        }

        fun makeOverdraw(colors: IntArray): ColorFilter {
            require(colors.size == 6) { "Expected 6 elements, got " + colors.size }
            return ColorFilter(ColorFilter_nMakeOverdraw(colors[0], colors[1], colors[2], colors[3], colors[4], colors[5]))
        }

        init {
            staticLoad()
        }

        val sRGBToLinearGamma = ColorFilter(ColorFilter_nGetSRGBToLinearGamma(), false)
        val luma = ColorFilter(ColorFilter_nGetLuma(), false)
    }

    internal constructor(ptr: NativePointer) : super(ptr)

    internal constructor(ptr: NativePointer, allowClose: Boolean) : super(ptr, allowClose)
}