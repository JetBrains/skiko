@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Managed
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skiko.RenderException
import org.jetbrains.skiko.loadOpenGLLibrary


@ExternalSymbolName("org_jetbrains_skia_BackendRenderTarget__1nGetFinalizer")
internal external fun BackendRenderTarget_nGetFinalizer(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_BackendRenderTarget__1nMakeGL")
internal external fun _nMakeGL(width: Int, height: Int, sampleCnt: Int, stencilBits: Int, fbId: Int, fbFormat: Int): NativePointer

@ExternalSymbolName("BackendRenderTarget_nMakeMetal")
internal external fun _nMakeMetal(width: Int, height: Int, texturePtr: NativePointer): NativePointer

@ExternalSymbolName("BackendRenderTarget_MakeDirect3D")
internal external fun _nMakeDirect3D(
    width: Int,
    height: Int,
    texturePtr: NativePointer,
    format: Int,
    sampleCnt: Int,
    levelCnt: Int
): NativePointer
