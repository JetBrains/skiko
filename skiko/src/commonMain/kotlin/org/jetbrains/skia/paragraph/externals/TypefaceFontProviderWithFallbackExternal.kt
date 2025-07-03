@file:org.jetbrains.skia.QuasiJsModule("./skiko.mjs")
@file:org.jetbrains.skia.QuasiJsNonModule
@file:org.jetbrains.skia.QuasiJsQualifier("wasmExports")
package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProviderWithFallback__1nMakeAsFallbackProvider")
internal external fun TypefaceFontProviderWithFallback_nMakeAsFallbackProvider(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProviderWithFallback__1nRegisterTypefaceForFallback")
internal external fun TypefaceFontProviderWithFallback_nRegisterTypefaceForFallback(
    ptr: NativePointer,
    typefacePtr: NativePointer,
    alias: InteropPointer
): Int
