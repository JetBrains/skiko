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


@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeComposed")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeComposed")
private external fun _nMakeComposed(outer: NativePointer, inner: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeBlend")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeBlend")
private external fun _nMakeBlend(color: Int, blendMode: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeMatrix")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeMatrix")
private external fun _nMakeMatrix(rowMajor: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeHSLAMatrix")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeHSLAMatrix")
private external fun _nMakeHSLAMatrix(rowMajor: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nGetLinearToSRGBGamma")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nGetLinearToSRGBGamma")
private external fun _nGetLinearToSRGBGamma(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nGetSRGBToLinearGamma")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nGetSRGBToLinearGamma")
private external fun _nGetSRGBToLinearGamma(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeLerp")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeLerp")
private external fun _nMakeLerp(t: Float, dstPtr: NativePointer, srcPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeLighting")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeLighting")
private external fun _nMakeLighting(colorMul: Int, colorAdd: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeHighContrast")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeHighContrast")
private external fun _nMakeHighContrast(grayscale: Boolean, inversionMode: Int, contrast: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeTable")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeTable")
private external fun _nMakeTable(table: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeOverdraw")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeOverdraw")
private external fun _nMakeOverdraw(c0: Int, c1: Int, c2: Int, c3: Int, c4: Int, c5: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nGetLuma")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nGetLuma")
private external fun _nGetLuma(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeTableARGB")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_ColorFilter__1nMakeTableARGB")
private external fun _nMakeTableARGB(
    a: InteropPointer,
    r: InteropPointer,
    g: InteropPointer,
    b: InteropPointer,
): NativePointer
