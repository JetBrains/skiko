package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.*

class ColorFilter : RefCnt {
    companion object {
        fun makeComposed(outer: ColorFilter?, inner: ColorFilter?): ColorFilter {
            return try {
                Stats.onNativeCall()
                ColorFilter(
                    _nMakeComposed(
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
            return ColorFilter(_nMakeBlend(color, mode.ordinal))
        }

        fun makeMatrix(matrix: ColorMatrix): ColorFilter {
            Stats.onNativeCall()
            return ColorFilter(
                interopScope {
                    _nMakeMatrix(toInterop(matrix.mat))
                }
            )
        }

        fun makeHSLAMatrix(matrix: ColorMatrix): ColorFilter {
            Stats.onNativeCall()
            return ColorFilter(
                interopScope {
                    _nMakeHSLAMatrix(toInterop(matrix.mat))
                }
            )
        }

        fun makeLerp(dst: ColorFilter?, src: ColorFilter?, t: Float): ColorFilter {
            return try {
                ColorFilter(
                    _nMakeLerp(
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
            return ColorFilter(_nMakeLighting(colorMul, colorAdd))
        }

        fun makeHighContrast(grayscale: Boolean, mode: InversionMode, contrast: Float): ColorFilter {
            return ColorFilter(_nMakeHighContrast(grayscale, mode.ordinal, contrast))
        }

        fun makeTable(table: ByteArray): ColorFilter {
            require(table.size == 256) { "Expected 256 elements, got " + table.size }
            return ColorFilter(
                interopScope {
                    _nMakeTable(toInterop(table))
                }
            )
        }

        fun makeTableARGB(a: ByteArray?, r: ByteArray?, g: ByteArray?, b: ByteArray?): ColorFilter {
            require(a == null || a.size == 256) { "Expected 256 elements in a[], got " + a!!.size }
            require(r == null || r.size == 256) { "Expected 256 elements in r[], got " + r!!.size }
            require(g == null || g.size == 256) { "Expected 256 elements in g[], got " + g!!.size }
            require(b == null || b.size == 256) { "Expected 256 elements in b[], got " + b!!.size }
            interopScope {
                return ColorFilter(_nMakeTableARGB(
                    toInterop(a),
                    toInterop(r),
                    toInterop(g),
                    toInterop(b),
                ))
            }
        }

        fun makeOverdraw(colors: IntArray): ColorFilter {
            require(colors.size == 6) { "Expected 6 elements, got " + colors.size }
            return ColorFilter(_nMakeOverdraw(colors[0], colors[1], colors[2], colors[3], colors[4], colors[5]))
        }

        init {
            staticLoad()
        }

        val sRGBToLinearGamma = ColorFilter(_nGetSRGBToLinearGamma(), false)
        val luma = ColorFilter(_nGetLuma(), false)
    }

    internal constructor(ptr: NativePointer) : super(ptr)

    internal constructor(ptr: NativePointer, allowClose: Boolean) : super(ptr, allowClose)
}