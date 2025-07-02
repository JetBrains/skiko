@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer


@ExternalSymbolName("org_jetbrains_skia_BackendRenderTarget__1nGetFinalizer")
internal external fun BackendRenderTarget_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_BackendRenderTarget__1nMakeGL")
internal external fun BackendRenderTarget_nMakeGL(width: Int, height: Int, sampleCnt: Int, stencilBits: Int, fbId: Int, fbFormat: Int): NativePointer

@ExternalSymbolName("BackendRenderTarget_nMakeMetal")
internal external fun BackendRenderTarget_nMakeMetal(width: Int, height: Int, texturePtr: NativePointer): NativePointer

@ExternalSymbolName("BackendRenderTarget_MakeDirect3D")
internal external fun BackendRenderTarget_nMakeDirect3D(
    width: Int,
    height: Int,
    texturePtr: NativePointer,
    format: Int,
    sampleCnt: Int,
    levelCnt: Int
): NativePointer
