@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorSpace__1nGetFinalizer")
internal external fun ColorSpace_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorSpace__nConvert")
internal external fun ColorSpace_nConvert(fromPtr: NativePointer, toPtr: NativePointer, r: Float, g: Float, b: Float, a: Float, result: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_ColorSpace__1nMakeSRGB")
internal external fun ColorSpace_nMakeSRGB(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorSpace__1nMakeDisplayP3")
internal external fun ColorSpace_nMakeDisplayP3(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorSpace__1nMakeSRGBLinear")
internal external fun ColorSpace_nMakeSRGBLinear(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_ColorSpace__1nIsGammaCloseToSRGB")
internal external fun ColorSpace_nIsGammaCloseToSRGB(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_ColorSpace__1nIsGammaLinear")
internal external fun ColorSpace_nIsGammaLinear(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_ColorSpace__1nIsSRGB")
internal external fun ColorSpace_nIsSRGB(ptr: NativePointer): Boolean
