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

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetFontCacheLimit")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_GraphicsKt__1nGetFontCacheLimit")
private external fun _nGetFontCacheLimit(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nSetFontCacheLimit")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_GraphicsKt__1nSetFontCacheLimit")
private external fun _nSetFontCacheLimit(bytes: Int): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetFontCacheUsed")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_GraphicsKt__1nGetFontCacheUsed")
private external fun _nGetFontCacheUsed(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetFontCacheCountLimit")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_GraphicsKt__1nGetFontCacheCountLimit")
private external fun _nGetFontCacheCountLimit(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nSetFontCacheCountLimit")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_GraphicsKt__1nSetFontCacheCountLimit")
private external fun _nSetFontCacheCountLimit(count: Int): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetFontCacheCountUsed")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_GraphicsKt__1nGetFontCacheCountUsed")
private external fun _nGetFontCacheCountUsed(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetResourceCacheTotalByteLimit")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_GraphicsKt__1nGetResourceCacheTotalByteLimit")
private external fun _nGetResourceCacheTotalByteLimit(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nSetResourceCacheTotalByteLimit")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_GraphicsKt__1nSetResourceCacheTotalByteLimit")
private external fun _nSetResourceCacheTotalByteLimit(bytes: Int): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetResourceCacheSingleAllocationByteLimit")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_GraphicsKt__1nGetResourceCacheSingleAllocationByteLimit")
private external fun _nGetResourceCacheSingleAllocationByteLimit(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nSetResourceCacheSingleAllocationByteLimit")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_GraphicsKt__1nSetResourceCacheSingleAllocationByteLimit")
private external fun _nSetResourceCacheSingleAllocationByteLimit(bytes: Int): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nGetResourceCacheTotalBytesUsed")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_GraphicsKt__1nGetResourceCacheTotalBytesUsed")
private external fun _nGetResourceCacheTotalBytesUsed(): Int

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nPurgeFontCache")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_GraphicsKt__1nPurgeFontCache")
private external fun _nPurgeFontCache()

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nPurgeResourceCache")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_GraphicsKt__1nPurgeResourceCache")
private external fun _nPurgeResourceCache()

@ExternalSymbolName("org_jetbrains_skia_GraphicsKt__1nPurgeAllCaches")
@ModuleImport("./skiko.mjs", "org_jetbrains_skia_GraphicsKt__1nPurgeAllCaches")
private external fun _nPurgeAllCaches()
