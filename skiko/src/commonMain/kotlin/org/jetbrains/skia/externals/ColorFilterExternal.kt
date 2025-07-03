@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")

package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeComposed")
internal external fun ColorFilter_nMakeComposed(outer: NativePointer, inner: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeBlend")
internal external fun ColorFilter_nMakeBlend(color: Int, blendMode: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeMatrix")
internal external fun ColorFilter_nMakeMatrix(rowMajor: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeHSLAMatrix")
internal external fun ColorFilter_nMakeHSLAMatrix(rowMajor: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nGetLinearToSRGBGamma")
internal external fun ColorFilter_nGetLinearToSRGBGamma(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nGetSRGBToLinearGamma")
internal external fun ColorFilter_nGetSRGBToLinearGamma(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeLerp")
internal external fun ColorFilter_nMakeLerp(t: Float, dstPtr: NativePointer, srcPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeLighting")
internal external fun ColorFilter_nMakeLighting(colorMul: Int, colorAdd: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeHighContrast")
internal external fun ColorFilter_nMakeHighContrast(grayscale: Boolean, inversionMode: Int, contrast: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeTable")
internal external fun ColorFilter_nMakeTable(table: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeOverdraw")
internal external fun ColorFilter_nMakeOverdraw(c0: Int, c1: Int, c2: Int, c3: Int, c4: Int, c5: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nGetLuma")
internal external fun ColorFilter_nGetLuma(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorFilter__1nMakeTableARGB")
internal external fun ColorFilter_nMakeTableARGB(
    a: InteropPointer,
    r: InteropPointer,
    g: InteropPointer,
    b: InteropPointer,
): NativePointer
