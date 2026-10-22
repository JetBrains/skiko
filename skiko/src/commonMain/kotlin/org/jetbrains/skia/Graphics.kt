package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.Stats

class Graphics {
    companion object {
        init {
            staticLoad()
        }

        /**
         * The max number of bytes that should be used by the font cache.
         */
        var fontCacheLimit: Int
            get() {
                Stats.onNativeCall()
                return _nGetFontCacheLimit()
            }
            set(value) {
                Stats.onNativeCall()
                _nSetFontCacheLimit(value)
            }

        /**
         * The number of bytes currently used by the font cache.
         */
        val fontCacheUsed: Int
            get() {
                Stats.onNativeCall()
                return _nGetFontCacheUsed()
            }

        /**
         * The max number of entries in the font cache.
         */
        var fontCacheCountLimit: Int
            get() {
                Stats.onNativeCall()
                return _nGetFontCacheCountLimit()
            }
            set(value) {
                Stats.onNativeCall()
                _nSetFontCacheCountLimit(value)
            }

        /**
         * The number of entries currently in the font cache.
         */
        val fontCacheCountUsed: Int
            get() {
                Stats.onNativeCall()
                return _nGetFontCacheCountUsed()
            }

        /**
         *  The memory usage limit in bytes for the resource cache, used for temporary bitmaps and other resources.
         *
         *  Entries are purged from the cache when the memory useage exceeds this limit.
         */
        var resourceCacheTotalLimit: Int
            get() {
                Stats.onNativeCall()
                return _nGetResourceCacheTotalByteLimit()
            }
            set(value) {
                Stats.onNativeCall()
                _nSetResourceCacheTotalByteLimit(value)
            }

        /**
         *  When the cachable entry is very large (e.g. a large scaled bitmap), adding it to the cache
         *  can cause most/all of the existing entries to be purged. To avoid this, the client can set
         *  a limit for a single allocation. If a cacheable entry would have been cached, but its size
         *  exceeds this limit, then we do not attempt to cache it at all.
         *
         *  Zero is the default value, meaning we always attempt to cache entries.
         */
        var resourceCacheSingleAllocationByteLimit: Int
            get() {
                Stats.onNativeCall()
                return _nGetResourceCacheSingleAllocationByteLimit()
            }
            set(value) {
                Stats.onNativeCall()
                _nSetResourceCacheSingleAllocationByteLimit(value)
            }

        /**
         * The memory in bytes used for temporary images and other resources.
         */
        val resourceCacheTotalUsed: Int
            get() {
                Stats.onNativeCall()
                return _nGetResourceCacheTotalBytesUsed()
            }

        /**
         * For debugging purposes, this will attempt to purge the font cache.
         *
         * It does not change the limit, but will cause subsequent font measures and draws to be recreated,
         * since they will no longer be in the cache.
         */
        fun purgeFontCache() {
            Stats.onNativeCall()
            _nPurgeFontCache()
        }

        /**
         * For debugging purposes, this will attempt to purge the resource cache.
         *
         * It does not change the limit.
         */
        fun purgeResourceCache() {
            Stats.onNativeCall()
            _nPurgeResourceCache()
        }

        /**
         *  Free as much globally cached memory as possible. This will purge all private caches in Skia,
         *  including font and image caches.
         *
         *  If there are caches associated with GPU context, those will not be affected by this call.
         */
        fun purgeAllCaches() {
            Stats.onNativeCall()
            _nPurgeAllCaches()
        }
    }
}

// Native: SkStrikeCache::getCacheSizeLimit takes fLock, held across strike creation -> SkScalerContext_Mac CTFont
@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetFontCacheLimit")
@ForceNativeThreadState
private external fun _nGetFontCacheLimit(): Int

// Native: SkStrikeCache::setCacheSizeLimit -> internalPurge -> ~SkStrike -> ~SkScalerContext_Mac releases CTFont
@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nSetFontCacheLimit")
@ForceNativeThreadState
private external fun _nSetFontCacheLimit(bytes: Int): Int

// Native: SkStrikeCache::getTotalMemoryUsed takes fLock, held across strike creation -> SkScalerContext_Mac CTFont
@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetFontCacheUsed")
@ForceNativeThreadState
private external fun _nGetFontCacheUsed(): Int

// Native: SkStrikeCache::getCacheCountLimit takes fLock, held across strike creation -> SkScalerContext_Mac CTFont
@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetFontCacheCountLimit")
@ForceNativeThreadState
private external fun _nGetFontCacheCountLimit(): Int

// Native: SkStrikeCache::setCacheCountLimit -> internalPurge -> ~SkStrike -> ~SkScalerContext_Mac releases CTFont
@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nSetFontCacheCountLimit")
@ForceNativeThreadState
private external fun _nSetFontCacheCountLimit(count: Int): Int

// Native: SkStrikeCache::getCacheCountUsed takes fLock, held across strike creation -> SkScalerContext_Mac CTFont
@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetFontCacheCountUsed")
@ForceNativeThreadState
private external fun _nGetFontCacheCountUsed(): Int

// Native: SkResourceCache::getTotalByteLimit takes fMutex, held across ~Rec -> ~SkPicture -> typeface release
@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetResourceCacheTotalByteLimit")
@ForceNativeThreadState
private external fun _nGetResourceCacheTotalByteLimit(): Int

// Native: SkResourceCache::setTotalByteLimit locks fMutex; purgeAsNeeded -> ~Rec -> ~SkPicture -> typeface
@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nSetResourceCacheTotalByteLimit")
@ForceNativeThreadState
private external fun _nSetResourceCacheTotalByteLimit(bytes: Int): Int

// Native: SkResourceCache::getSingleAllocationByteLimit takes fMutex, held across ~Rec -> typeface release
@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetResourceCacheSingleAllocationByteLimit")
@ForceNativeThreadState
private external fun _nGetResourceCacheSingleAllocationByteLimit(): Int

// Native: SkResourceCache::setSingleAllocationByteLimit takes fMutex, held across ~Rec -> typeface release
@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nSetResourceCacheSingleAllocationByteLimit")
@ForceNativeThreadState
private external fun _nSetResourceCacheSingleAllocationByteLimit(bytes: Int): Int

// Native: SkResourceCache::getTotalBytesUsed takes fMutex, held across ~Rec -> ~SkPicture -> typeface release
@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetResourceCacheTotalBytesUsed")
@ForceNativeThreadState
private external fun _nGetResourceCacheTotalBytesUsed(): Int

// Native: SkStrikeCache::purgeAll -> ~SkScalerContext_Mac; SkTypefaceCache::PurgeAll drops SkTypeface_Mac
@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nPurgeFontCache")
@ForceNativeThreadState
private external fun _nPurgeFontCache()

// Native: SkResourceCache::PurgeAll -> purgeAsNeeded -> ~Rec -> ~SkPicture -> ~SkTextBlob -> typeface release
@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nPurgeResourceCache")
@ForceNativeThreadState
private external fun _nPurgeResourceCache()

// Native: SkStrikeCache::purgeAll -> ~SkScalerContext_Mac; SkTypefaceCache::PurgeAll drops SkTypeface_Mac
@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nPurgeAllCaches")
@ForceNativeThreadState
private external fun _nPurgeAllCaches()
