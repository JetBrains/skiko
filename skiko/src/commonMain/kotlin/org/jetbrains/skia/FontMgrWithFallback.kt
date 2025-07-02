package org.jetbrains.skia

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
) : FontMgr(FontMgrWithFallback_nDefaultWithFallbackFontProvider(fallbackProvider._ptr))
