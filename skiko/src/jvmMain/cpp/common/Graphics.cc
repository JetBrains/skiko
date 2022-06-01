#include <iostream>
#include <jni.h>
#include "SkGraphics.h"
#include "interop.hh"

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsKt__1nGetFontCacheLimit
  (JNIEnv* env, jclass jclass) {
  return SkGraphics::GetFontCacheLimit();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsKt__1nSetFontCacheLimit
  (JNIEnv* env, jclass jclass, jint bytes) {
  return SkGraphics::SetFontCacheLimit(bytes);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsKt__1nGetFontCacheUsed
  (JNIEnv* env, jclass jclass) {
  return SkGraphics::GetFontCacheUsed();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsKt__1nGetFontCacheCountLimit
  (JNIEnv* env, jclass jclass) {
  return SkGraphics::GetFontCacheCountLimit();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsKt__1nSetFontCacheCountLimit
  (JNIEnv* env, jclass jclass, jint count) {
  return SkGraphics::SetFontCacheCountLimit(count);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsKt__1nGetFontCacheCountUsed
  (JNIEnv* env, jclass jclass) {
  return SkGraphics::GetFontCacheCountUsed();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsKt__1nGetResourceCacheTotalByteLimit
  (JNIEnv* env, jclass jclass) {
  return SkGraphics::GetResourceCacheTotalByteLimit();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsKt__1nSetResourceCacheTotalByteLimit
  (JNIEnv* env, jclass jclass, jint bytes) {
  return SkGraphics::SetResourceCacheTotalByteLimit(bytes);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsKt__1nGetResourceCacheSingleAllocationByteLimit
  (JNIEnv* env, jclass jclass) {
  return SkGraphics::GetResourceCacheSingleAllocationByteLimit();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsKt__1nSetResourceCacheSingleAllocationByteLimit
  (JNIEnv* env, jclass jclass, jint bytes) {
  return SkGraphics::SetResourceCacheSingleAllocationByteLimit(bytes);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsKt__1nGetResourceCacheTotalBytesUsed
  (JNIEnv* env, jclass jclass) {
  return SkGraphics::GetResourceCacheTotalBytesUsed();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_GraphicsKt__1nPurgeFontCache
  (JNIEnv* env, jclass jclass) {
  SkGraphics::PurgeFontCache();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_GraphicsKt__1nPurgeResourceCache
  (JNIEnv* env, jclass jclass) {
  SkGraphics::PurgeResourceCache();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_GraphicsKt__1nPurgeAllCaches
  (JNIEnv* env, jclass jclass) {
  SkGraphics::PurgeAllCaches();
}
