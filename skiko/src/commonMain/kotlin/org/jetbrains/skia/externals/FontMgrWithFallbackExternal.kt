@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_FontMgrWithFallback__1nDefaultWithFallbackFontProvider")
internal external fun FontMgrWithFallback_nDefaultWithFallbackFontProvider(fallbackPtr: NativePointer): NativePointer
