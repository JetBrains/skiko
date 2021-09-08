@file:Suppress("NESTED_EXTERNAL_DECLARATION")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.reachabilityBarrier
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.getPtr
import kotlin.jvm.JvmStatic

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
            return ColorFilter(_nMakeTable(table))
        }

        fun makeTableARGB(a: ByteArray?, r: ByteArray?, g: ByteArray?, b: ByteArray?): ColorFilter {
            require(a == null || a.size == 256) { "Expected 256 elements in a[], got " + a!!.size }
            require(r == null || r.size == 256) { "Expected 256 elements in r[], got " + r!!.size }
            require(g == null || g.size == 256) { "Expected 256 elements in g[], got " + g!!.size }
            require(b == null || b.size == 256) { "Expected 256 elements in b[], got " + b!!.size }
            return ColorFilter(_nMakeTableARGB(a, r, g, b))
        }

        fun makeOverdraw(colors: IntArray): ColorFilter {
            require(colors.size == 6) { "Expected 6 elements, got " + colors.size }
            return ColorFilter(_nMakeOverdraw(colors[0], colors[1], colors[2], colors[3], colors[4], colors[5]))
        }

        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeComposed")
        external fun _nMakeComposed(outer: NativePointer, inner: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeBlend")
        external fun _nMakeBlend(color: Int, blendMode: Int): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeMatrix")
        external fun _nMakeMatrix(rowMajor: FloatArray?): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeHSLAMatrix")
        external fun _nMakeHSLAMatrix(rowMajor: FloatArray?): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nGetLinearToSRGBGamma")
        external fun _nGetLinearToSRGBGamma(): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nGetSRGBToLinearGamma")
        external fun _nGetSRGBToLinearGamma(): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeLerp")
        external fun _nMakeLerp(t: Float, dstPtr: NativePointer, srcPtr: NativePointer): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeLighting")
        external fun _nMakeLighting(colorMul: Int, colorAdd: Int): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeHighContrast")
        external fun _nMakeHighContrast(grayscale: Boolean, inversionMode: Int, contrast: Float): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeTable")
        external fun _nMakeTable(table: ByteArray?): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeTableARGB")
        external fun _nMakeTableARGB(a: ByteArray?, r: ByteArray?, g: ByteArray?, b: ByteArray?): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeOverdraw")
        external fun _nMakeOverdraw(c0: Int, c1: Int, c2: Int, c3: Int, c4: Int, c5: Int): NativePointer
        @JvmStatic
        @ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nGetLuma")
        external fun _nGetLuma(): NativePointer

        init {
            staticLoad()
        }

        val sRGBToLinearGamma = ColorFilter(_nGetSRGBToLinearGamma(), false)
        val luma = ColorFilter(_nGetLuma(), false)
    }

    internal constructor(ptr: NativePointer) : super(ptr)

    internal constructor(ptr: NativePointer, allowClose: Boolean) : super(ptr, allowClose)
}