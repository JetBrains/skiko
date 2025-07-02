@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nGetRowBytes")
internal external fun PixelRef_nGetRowBytes(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nGetGenerationId")
internal external fun PixelRef_nGetGenerationId(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nNotifyPixelsChanged")
internal external fun PixelRef_nNotifyPixelsChanged(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nIsImmutable")
internal external fun PixelRef_nIsImmutable(ptr: NativePointer): Boolean

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nSetImmutable")
internal external fun PixelRef_nSetImmutable(ptr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nGetWidth")
internal external fun PixelRef_nGetWidth(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_PixelRef__1nGetHeight")
internal external fun PixelRef_nGetHeight(ptr: NativePointer): Int
