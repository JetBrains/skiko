package org.jetbrains.skia

import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.paragraph.TypefaceFontProviderWithFallback

/**
 * Consider registering this FontMgr as a default one
 * to let all other possibly registered Font managers to look for their fallbacks first:
 * `FontCollection.setDefaultFontManager(...)`
 *
 * The fallbacks provided by this class will be used a last resort.
 */
class FontMgrWithFallback(
    fallbackProvider: TypefaceFontProviderWithFallback
) : FontMgr(_nDefaultWithFallbackFontProvider(fallbackProvider._ptr))

@ExternalSymbolName("org_jetbrains_skia_FontMgrWithFallback__1nDefaultWithFallbackFontProvider")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_FontMgrWithFallback__1nDefaultWithFallbackFontProvider")
private external fun _nDefaultWithFallbackFontProvider(fallbackPtr: NativePointer): NativePointer