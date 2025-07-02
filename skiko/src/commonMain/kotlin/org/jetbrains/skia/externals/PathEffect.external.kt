@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeCompose")
internal external fun PathEffect_nMakeCompose(outerPtr: NativePointer, innerPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeSum")
internal external fun PathEffect_nMakeSum(firstPtr: NativePointer, secondPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakePath1D")
internal external fun PathEffect_nMakePath1D(pathPtr: NativePointer, advance: Float, phase: Float, style: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakePath2D")
internal external fun PathEffect_nMakePath2D(matrix: InteropPointer, pathPtr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeLine2D")
internal external fun PathEffect_nMakeLine2D(width: Float, matrix: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeCorner")
internal external fun PathEffect_nMakeCorner(radius: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeDash")
internal external fun PathEffect_nMakeDash(intervals: InteropPointer, count: Int, phase: Float): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PathEffect__1nMakeDiscrete")
internal external fun PathEffect_nMakeDiscrete(segLength: Float, dev: Float, seed: Int): NativePointer
