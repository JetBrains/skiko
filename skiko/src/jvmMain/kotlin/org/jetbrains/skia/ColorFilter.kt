package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Native
import org.jetbrains.skia.impl.Stats
import java.lang.ref.Reference

class ColorFilter : RefCnt {
    companion object {
        fun makeComposed(outer: ColorFilter?, inner: ColorFilter?): ColorFilter {
            return try {
                Stats.onNativeCall()
                ColorFilter(
                    _nMakeComposed(
                        Native.Companion.getPtr(outer),
                        Native.Companion.getPtr(inner)
                    )
                )
            } finally {
                Reference.reachabilityFence(outer)
                Reference.reachabilityFence(inner)
            }
        }

        fun makeBlend(color: Int, mode: BlendMode): ColorFilter {
            Stats.onNativeCall()
            return ColorFilter(_nMakeBlend(color, mode.ordinal))
        }

        fun makeMatrix(matrix: ColorMatrix): ColorFilter {
            Stats.onNativeCall()
            return ColorFilter(_nMakeMatrix(matrix.mat))
        }

        fun makeHSLAMatrix(matrix: ColorMatrix): ColorFilter {
            Stats.onNativeCall()
            return ColorFilter(_nMakeHSLAMatrix(matrix.mat))
        }

        fun makeLerp(dst: ColorFilter?, src: ColorFilter?, t: Float): ColorFilter {
            return try {
                ColorFilter(
                    _nMakeLerp(
                        t,
                        Native.Companion.getPtr(dst),
                        Native.Companion.getPtr(src)
                    )
                )
            } finally {
                Reference.reachabilityFence(dst)
                Reference.reachabilityFence(src)
            }
        }

        fun makeLighting(colorMul: Int, colorAdd: Int): ColorFilter {
            return ColorFilter(_nMakeLighting(colorMul, colorAdd))
        }

        fun makeHighContrast(grayscale: Boolean, mode: InversionMode, contrast: Float): ColorFilter {
            return ColorFilter(_nMakeHighContrast(grayscale, mode.ordinal, contrast))
        }

        fun makeTable(table: ByteArray): ColorFilter {
            assert(table.size == 256) { "Expected 256 elements, got " + table.size }
            return ColorFilter(_nMakeTable(table))
        }

        fun makeTableARGB(a: ByteArray?, r: ByteArray?, g: ByteArray?, b: ByteArray?): ColorFilter {
            assert(a == null || a.size == 256) { "Expected 256 elements in a[], got " + a!!.size }
            assert(r == null || r.size == 256) { "Expected 256 elements in r[], got " + r!!.size }
            assert(g == null || g.size == 256) { "Expected 256 elements in g[], got " + g!!.size }
            assert(b == null || b.size == 256) { "Expected 256 elements in b[], got " + b!!.size }
            return ColorFilter(_nMakeTableARGB(a, r, g, b))
        }

        fun makeOverdraw(colors: IntArray): ColorFilter {
            assert(colors.size == 6) { "Expected 6 elements, got " + colors.size }
            return ColorFilter(_nMakeOverdraw(colors[0], colors[1], colors[2], colors[3], colors[4], colors[5]))
        }

        @JvmStatic external fun _nMakeComposed(outer: Long, inner: Long): Long
        @JvmStatic external fun _nMakeBlend(color: Int, blendMode: Int): Long
        @JvmStatic external fun _nMakeMatrix(rowMajor: FloatArray?): Long
        @JvmStatic external fun _nMakeHSLAMatrix(rowMajor: FloatArray?): Long
        @JvmStatic external fun _nGetLinearToSRGBGamma(): Long
        @JvmStatic external fun _nGetSRGBToLinearGamma(): Long
        @JvmStatic external fun _nMakeLerp(t: Float, dstPtr: Long, srcPtr: Long): Long
        @JvmStatic external fun _nMakeLighting(colorMul: Int, colorAdd: Int): Long
        @JvmStatic external fun _nMakeHighContrast(grayscale: Boolean, inversionMode: Int, contrast: Float): Long
        @JvmStatic external fun _nMakeTable(table: ByteArray?): Long
        @JvmStatic external fun _nMakeTableARGB(a: ByteArray?, r: ByteArray?, g: ByteArray?, b: ByteArray?): Long
        @JvmStatic external fun _nMakeOverdraw(c0: Int, c1: Int, c2: Int, c3: Int, c4: Int, c5: Int): Long
        @JvmStatic external fun _nGetLuma(): Long

        init {
            staticLoad()
        }

        val sRGBToLinearGamma = ColorFilter(_nGetSRGBToLinearGamma(), false)
        val luma = ColorFilter(_nGetLuma(), false)
    }

    internal constructor(ptr: Long) : super(ptr)

    internal constructor(ptr: Long, allowClose: Boolean) : super(ptr, allowClose)
}