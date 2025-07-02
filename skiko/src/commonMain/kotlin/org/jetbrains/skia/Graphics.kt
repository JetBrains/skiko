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
                return Graphics_nGetFontCacheLimit()
            }
            set(value) {
                Stats.onNativeCall()
                Graphics_nSetFontCacheLimit(value)
            }

        /**
         * The number of bytes currently used by the font cache.
         */
        val fontCacheUsed: Int
            get() {
                Stats.onNativeCall()
                return Graphics_nGetFontCacheUsed()
            }

        /**
         * The max number of entries in the font cache.
         */
        var fontCacheCountLimit: Int
            get() {
                Stats.onNativeCall()
                return Graphics_nGetFontCacheCountLimit()
            }
            set(value) {
                Stats.onNativeCall()
                Graphics_nSetFontCacheCountLimit(value)
            }

        /**
         * The number of entries currently in the font cache.
         */
        val fontCacheCountUsed: Int
            get() {
                Stats.onNativeCall()
                return Graphics_nGetFontCacheCountUsed()
            }

        /**
         *  The memory usage limit in bytes for the resource cache, used for temporary bitmaps and other resources.
         *
         *  Entries are purged from the cache when the memory useage exceeds this limit.
         */
        var resourceCacheTotalLimit: Int
            get() {
                Stats.onNativeCall()
                return Graphics_nGetResourceCacheTotalByteLimit()
            }
            set(value) {
                Stats.onNativeCall()
                Graphics_nSetResourceCacheTotalByteLimit(value)
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
                return Graphics_nGetResourceCacheSingleAllocationByteLimit()
            }
            set(value) {
                Stats.onNativeCall()
                Graphics_nSetResourceCacheSingleAllocationByteLimit(value)
            }

        /**
         * The memory in bytes used for temporary images and other resources.
         */
        val resourceCacheTotalUsed: Int
            get() {
                Stats.onNativeCall()
                return Graphics_nGetResourceCacheTotalBytesUsed()
            }

        /**
         * For debugging purposes, this will attempt to purge the font cache.
         *
         * It does not change the limit, but will cause subsequent font measures and draws to be recreated,
         * since they will no longer be in the cache.
         */
        fun purgeFontCache() {
            Stats.onNativeCall()
            Graphics_nPurgeFontCache()
        }

        /**
         * For debugging purposes, this will attempt to purge the resource cache.
         *
         * It does not change the limit.
         */
        fun purgeResourceCache() {
            Stats.onNativeCall()
            Graphics_nPurgeResourceCache()
        }

        /**
         *  Free as much globally cached memory as possible. This will purge all private caches in Skia,
         *  including font and image caches.
         *
         *  If there are caches associated with GPU context, those will not be affected by this call.
         */
        fun purgeAllCaches() {
            Stats.onNativeCall()
            Graphics_nPurgeAllCaches()
        }
    }
}