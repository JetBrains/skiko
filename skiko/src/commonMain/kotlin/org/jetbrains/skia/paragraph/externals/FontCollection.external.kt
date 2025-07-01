package org.jetbrains.skia.paragraph

import org.jetbrains.skia.ExternalSymbolName
import org.jetbrains.skia.ModuleImport
import org.jetbrains.skia.impl.InteropPointer
import org.jetbrains.skia.impl.NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nMake")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_FontCollection__1nMake")
internal external fun FontCollection_nMake(): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nGetFontManagersCount")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_FontCollection__1nGetFontManagersCount")
internal external fun FontCollection_nGetFontManagersCount(ptr: NativePointer): Int

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetAssetFontManager")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_FontCollection__1nSetAssetFontManager")
internal external fun FontCollection_nSetAssetFontManager(ptr: NativePointer, fontManagerPtr: NativePointer, defaultFamilyNameStr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetDynamicFontManager")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_FontCollection__1nSetDynamicFontManager")
internal external fun FontCollection_nSetDynamicFontManager(ptr: NativePointer, fontManagerPtr: NativePointer, defaultFamilyNameStr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetTestFontManager")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_FontCollection__1nSetTestFontManager")
internal external fun FontCollection_nSetTestFontManager(ptr: NativePointer, fontManagerPtr: NativePointer, defaultFamilyNameStr: NativePointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetDefaultFontManager")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_FontCollection__1nSetDefaultFontManager")
internal external fun FontCollection_nSetDefaultFontManager(ptr: NativePointer, fontManagerPtr: NativePointer, defaultFamilyName: InteropPointer)

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nGetFallbackManager")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_FontCollection__1nGetFallbackManager")
internal external fun FontCollection_nGetFallbackManager(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nFindTypefaces")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_FontCollection__1nFindTypefaces")
internal external fun FontCollection_nFindTypefaces(ptr: NativePointer, familyNames: InteropPointer, len: Int, fontStyle: Int): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallbackChar")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallbackChar")
internal external fun FontCollection_nDefaultFallbackChar(ptr: NativePointer, unicode: Int, fontStyle: Int, locale: InteropPointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallback")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_FontCollection__1nDefaultFallback")
internal external fun FontCollection_nDefaultFallback(ptr: NativePointer): NativePointer

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nSetEnableFallback")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_FontCollection__1nSetEnableFallback")
internal external fun FontCollection_nSetEnableFallback(ptr: NativePointer, value: Boolean)

@ExternalSymbolName("org_jetbrains_skia_paragraph_FontCollection__1nGetParagraphCache")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_paragraph_FontCollection__1nGetParagraphCache")
internal external fun FontCollection_nGetParagraphCache(ptr: NativePointer): NativePointer
