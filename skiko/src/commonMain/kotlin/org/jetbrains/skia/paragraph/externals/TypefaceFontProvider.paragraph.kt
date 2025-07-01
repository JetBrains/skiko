package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProvider__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TypefaceFontProvider__1nMake")
internal external fun TypefaceFontProvider_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypeface")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_TypefaceFontProvider__1nRegisterTypeface")
internal external fun TypefaceFontProvider_nRegisterTypeface(ptr: NativePointer, typefacePtr: NativePointer, alias: InteropPointer): Int
