package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skia.impl.nativeCallFromStatic
import kotlin.jvm.JvmStatic

class Graphics {
    companion object {
        init {
            staticLoad()
        }

        /**
         * The max number of bytes that should be used by the font cache.
         */
        var fontCacheLimit: Int
            get() = nativeCallFromStatic { _nGetFontCacheLimit() }
            set(value) = nativeCallFromStatic { _nSetFontCacheLimit(value) }

        /**
         * The number of bytes currently used by the font cache.
         */
        val fontCacheUsed: Int
            get() = nativeCallFromStatic { _nGetFontCacheUsed() }

        /**
         * The max number of entries in the font cache.
         */
        var fontCacheCountLimit: Int
            get() = nativeCallFromStatic { _nGetFontCacheCountLimit() }
            set(value) = nativeCallFromStatic { _nSetFontCacheCountLimit(value) }

        /**
         * The number of entries currently in the font cache.
         */
        val fontCacheCountUsed: Int
            get() = nativeCallFromStatic { _nGetFontCacheCountUsed() }

        /**
         *  The memory usage limit in bytes for the resource cache, used for temporary bitmaps and other resources.
         *
         *  Entries are purged from the cache when the memory useage exceeds this limit.
         */
        var resourceCacheTotalLimit: Int
            get() = nativeCallFromStatic { _nGetResourceCacheTotalByteLimit() }
            set(value) = nativeCallFromStatic { _nSetResourceCacheTotalByteLimit(value) }

        /**
         *  When the cachable entry is very large (e.g. a large scaled bitmap), adding it to the cache
         *  can cause most/all of the existing entries to be purged. To avoid this, the client can set
         *  a limit for a single allocation. If a cacheable entry would have been cached, but its size
         *  exceeds this limit, then we do not attempt to cache it at all.
         *
         *  Zero is the default value, meaning we always attempt to cache entries.
         */
        var resourceCacheSingleAllocationByteLimit: Int
            get() = nativeCallFromStatic { _nGetResourceCacheSingleAllocationByteLimit() }
            set(value) = nativeCallFromStatic { _nSetResourceCacheSingleAllocationByteLimit(value) }

        /**
         * The memory in bytes used for temporary images and other resources.
         */
        val resourceCacheTotalUsed: Int
            get() = nativeCallFromStatic { _nGetResourceCacheTotalBytesUsed() }

        /**
         * For debugging purposes, this will attempt to purge the font cache.
         *
         * It does not change the limit, but will cause subsequent font measures and draws to be recreated,
         * since they will no longer be in the cache.
         */
        @JvmStatic
        fun purgeFontCache() = nativeCallFromStatic { _nPurgeFontCache() }

        /**
         * For debugging purposes, this will attempt to purge the resource cache.
         *
         * It does not change the limit.
         */
        @JvmStatic
        fun purgeResourceCache() = nativeCallFromStatic { _nPurgeResourceCache() }

        /**
         *  Free as much globally cached memory as possible. This will purge all private caches in Skia,
         *  including font and image caches.
         *
         *  If there are caches associated with GPU context, those will not be affected by this call.
         */
        @JvmStatic
        fun purgeAllCaches() = nativeCallFromStatic { _nPurgeAllCaches() }
    }
}

// <editor-fold defaultstate="collapsed" desc="Native External Functions">
@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetFontCacheLimit")
// Needs ForceNativeThreadState: takes a font-cache lock that stays held while CoreText fonts are created, so it can
// wait for another thread.
// Native call path: SkStrikeCache::getCacheSizeLimit
@ForceNativeThreadState
private external fun _nGetFontCacheLimit(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nSetFontCacheLimit")
// Needs ForceNativeThreadState: can free cached font data, which releases CoreText font objects.
// Native call path: SkStrikeCache::setCacheSizeLimit -> internalPurge -> ~SkStrike -> ~SkScalerContext_Mac
@ForceNativeThreadState
private external fun _nSetFontCacheLimit(bytes: Int): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetFontCacheUsed")
// Needs ForceNativeThreadState: takes a font-cache lock that stays held while CoreText fonts are created, so it can
// wait for another thread.
// Native call path: SkStrikeCache::getTotalMemoryUsed
@ForceNativeThreadState
private external fun _nGetFontCacheUsed(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetFontCacheCountLimit")
// Needs ForceNativeThreadState: takes a font-cache lock that stays held while CoreText fonts are created, so it can
// wait for another thread.
// Native call path: SkStrikeCache::getCacheCountLimit
@ForceNativeThreadState
private external fun _nGetFontCacheCountLimit(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nSetFontCacheCountLimit")
// Needs ForceNativeThreadState: can free cached font data, which releases CoreText font objects.
// Native call path: SkStrikeCache::setCacheCountLimit -> internalPurge -> ~SkStrike -> ~SkScalerContext_Mac
@ForceNativeThreadState
private external fun _nSetFontCacheCountLimit(count: Int): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetFontCacheCountUsed")
// Needs ForceNativeThreadState: takes a font-cache lock that stays held while CoreText fonts are created, so it can
// wait for another thread.
// Native call path: SkStrikeCache::getCacheCountUsed
@ForceNativeThreadState
private external fun _nGetFontCacheCountUsed(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetResourceCacheTotalByteLimit")
// Needs ForceNativeThreadState: can wait on a cache lock held while another thread frees pictures and releases fonts
// through CoreFoundation.
// Native call path: SkResourceCache::getTotalByteLimit
@ForceNativeThreadState
private external fun _nGetResourceCacheTotalByteLimit(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nSetResourceCacheTotalByteLimit")
// Needs ForceNativeThreadState: takes a cache lock and can free cached pictures when the limit shrinks, and freeing one
// can release a font.
// Native call path: SkResourceCache::setTotalByteLimit -> purgeAsNeeded -> ~Rec -> ~SkPicture
@ForceNativeThreadState
private external fun _nSetResourceCacheTotalByteLimit(bytes: Int): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetResourceCacheSingleAllocationByteLimit")
// Needs ForceNativeThreadState: can wait on a cache lock held while another thread frees pictures and releases fonts
// through CoreFoundation.
// Native call path: SkResourceCache::getSingleAllocationByteLimit
@ForceNativeThreadState
private external fun _nGetResourceCacheSingleAllocationByteLimit(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nSetResourceCacheSingleAllocationByteLimit")
// Needs ForceNativeThreadState: can wait on a cache lock held while another thread frees pictures and releases fonts
// through CoreFoundation.
// Native call path: SkResourceCache::setSingleAllocationByteLimit
@ForceNativeThreadState
private external fun _nSetResourceCacheSingleAllocationByteLimit(bytes: Int): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetResourceCacheTotalBytesUsed")
// Needs ForceNativeThreadState: can wait on a cache lock held while another thread frees pictures and releases fonts
// through CoreFoundation.
// Native call path: SkResourceCache::getTotalBytesUsed
@ForceNativeThreadState
private external fun _nGetResourceCacheTotalBytesUsed(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nPurgeFontCache")
// Needs ForceNativeThreadState: frees all cached font data, which releases CoreText font objects.
// Native call path: SkStrikeCache::purgeAll -> ~SkScalerContext_Mac
@ForceNativeThreadState
private external fun _nPurgeFontCache()

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nPurgeResourceCache")
// Needs ForceNativeThreadState: frees all cached pictures and their text, which can release a font, while holding the
// cache lock.
// Native call path: SkResourceCache::PurgeAll -> purgeAsNeeded -> ~Rec -> ~SkPicture -> ~SkTextBlob
@ForceNativeThreadState
private external fun _nPurgeResourceCache()

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nPurgeAllCaches")
// Needs ForceNativeThreadState: frees all font caches and picture caches, which can release CoreText font objects.
// Native call path: SkStrikeCache::purgeAll -> ~SkScalerContext_Mac
@ForceNativeThreadState
private external fun _nPurgeAllCaches()
// </editor-fold>