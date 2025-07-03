@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeShader")
internal external fun RuntimeEffect_nMakeShader(
    runtimeEffectPtr: NativePointer, uniformPtr: NativePointer, childrenPtrs: InteropPointer,
    childCount: Int, localMatrix: InteropPointer
): NativePointer


@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeForShader")
internal external fun RuntimeEffect_nMakeForShader(sksl: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1nMakeForColorFilter")
internal external fun RuntimeEffect_nMakeForColorFilter(sksl: InteropPointer): NativePointer

//  The functions below can be used only in JS and native targets

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1Result_nGetPtr")
internal external fun Result_nGetPtr(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1Result_nGetError")
internal external fun Result_nGetError(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_RuntimeEffect__1Result_nDestroy")
internal external fun Result_nDestroy(ptr: NativePointer)
