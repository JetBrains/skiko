package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProviderWithFallback__1nMakeAsFallbackProvider")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TypefaceFontProviderWithFallback__1nMakeAsFallbackProvider")
internal external fun TypefaceFontProviderWithFallback_nMakeAsFallbackProvider(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProviderWithFallback__1nRegisterTypefaceForFallback")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TypefaceFontProviderWithFallback__1nRegisterTypefaceForFallback")
internal external fun TypefaceFontProviderWithFallback_nRegisterTypefaceForFallback(
    ptr: NativePointer,
    typefacePtr: NativePointer,
    alias: InteropPointer
): Int