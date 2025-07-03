@file:QuasiJsModule("./skiko.mjs")
@file:QuasiJsNonModule
@file:QuasiJsQualifier("wasmExports")
package org.jetbrains.skia

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetFontCacheLimit")
internal external fun Graphics_nGetFontCacheLimit(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nSetFontCacheLimit")
internal external fun Graphics_nSetFontCacheLimit(bytes: Int): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetFontCacheUsed")
internal external fun Graphics_nGetFontCacheUsed(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetFontCacheCountLimit")
internal external fun Graphics_nGetFontCacheCountLimit(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nSetFontCacheCountLimit")
internal external fun Graphics_nSetFontCacheCountLimit(count: Int): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetFontCacheCountUsed")
internal external fun Graphics_nGetFontCacheCountUsed(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetResourceCacheTotalByteLimit")
internal external fun Graphics_nGetResourceCacheTotalByteLimit(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nSetResourceCacheTotalByteLimit")
internal external fun Graphics_nSetResourceCacheTotalByteLimit(bytes: Int): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetResourceCacheSingleAllocationByteLimit")
internal external fun Graphics_nGetResourceCacheSingleAllocationByteLimit(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nSetResourceCacheSingleAllocationByteLimit")
internal external fun Graphics_nSetResourceCacheSingleAllocationByteLimit(bytes: Int): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetResourceCacheTotalBytesUsed")
internal external fun Graphics_nGetResourceCacheTotalBytesUsed(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nPurgeFontCache")
internal external fun Graphics_nPurgeFontCache()

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nPurgeResourceCache")
internal external fun Graphics_nPurgeResourceCache()

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nPurgeAllCaches")
internal external fun Graphics_nPurgeAllCaches()
