#include "SkGraphics.h"
#include "common.h"

SKIKO_EXPORT KInt org_jetbrains_skia_GraphicsKt__1nGetFontCacheLimit
  () {
  return SkGraphics::GetFontCacheLimit();
}

SKIKO_EXPORT KInt org_jetbrains_skia_GraphicsKt__1nSetFontCacheLimit
  (KInt bytes) {
  return SkGraphics::SetFontCacheLimit(bytes);
}

SKIKO_EXPORT KInt org_jetbrains_skia_GraphicsKt__1nGetFontCacheUsed
  () {
  return SkGraphics::GetFontCacheUsed();
}

SKIKO_EXPORT KInt org_jetbrains_skia_GraphicsKt__1nGetFontCacheCountLimit
  () {
  return SkGraphics::GetFontCacheCountLimit();
}

SKIKO_EXPORT KInt org_jetbrains_skia_GraphicsKt__1nSetFontCacheCountLimit
  (KInt count) {
  return SkGraphics::SetFontCacheCountLimit(count);
}

SKIKO_EXPORT KInt org_jetbrains_skia_GraphicsKt__1nGetFontCacheCountUsed
  () {
  return SkGraphics::GetFontCacheCountUsed();
}

SKIKO_EXPORT KInt org_jetbrains_skia_GraphicsKt__1nGetResourceCacheTotalByteLimit
  () {
  return SkGraphics::GetResourceCacheTotalByteLimit();
}

SKIKO_EXPORT KInt org_jetbrains_skia_GraphicsKt__1nSetResourceCacheTotalByteLimit
  (KInt bytes) {
  return SkGraphics::SetResourceCacheTotalByteLimit(bytes);
}

SKIKO_EXPORT KInt org_jetbrains_skia_GraphicsKt__1nGetResourceCacheSingleAllocationByteLimit
  () {
  return SkGraphics::GetResourceCacheSingleAllocationByteLimit();
}

SKIKO_EXPORT KInt org_jetbrains_skia_GraphicsKt__1nSetResourceCacheSingleAllocationByteLimit
  (KInt bytes) {
  return SkGraphics::SetResourceCacheSingleAllocationByteLimit(bytes);
}

SKIKO_EXPORT KInt org_jetbrains_skia_GraphicsKt__1nGetResourceCacheTotalBytesUsed
  () {
  return SkGraphics::GetResourceCacheTotalBytesUsed();
}

SKIKO_EXPORT void org_jetbrains_skia_GraphicsKt__1nPurgeFontCache
  () {
  SkGraphics::PurgeFontCache();
}

SKIKO_EXPORT void org_jetbrains_skia_GraphicsKt__1nPurgeResourceCache
  () {
  SkGraphics::PurgeResourceCache();
}

SKIKO_EXPORT void org_jetbrains_skia_GraphicsKt__1nPurgeAllCaches
  () {
  SkGraphics::PurgeAllCaches();
}
