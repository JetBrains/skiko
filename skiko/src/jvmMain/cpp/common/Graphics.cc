#include <iostream>
#include <jni.h>
#include "SkGraphics.h"
#include "interop.hh"

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsExternalKt_Graphics_1nGetFontCacheLimit
  (JNIEnv* env, jclass jclass) {
  return SkGraphics::GetFontCacheLimit();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsExternalKt_Graphics_1nSetFontCacheLimit
  (JNIEnv* env, jclass jclass, jint bytes) {
  return SkGraphics::SetFontCacheLimit(bytes);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsExternalKt_Graphics_1nGetFontCacheUsed
  (JNIEnv* env, jclass jclass) {
  return SkGraphics::GetFontCacheUsed();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsExternalKt_Graphics_1nGetFontCacheCountLimit
  (JNIEnv* env, jclass jclass) {
  return SkGraphics::GetFontCacheCountLimit();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsExternalKt_Graphics_1nSetFontCacheCountLimit
  (JNIEnv* env, jclass jclass, jint count) {
  return SkGraphics::SetFontCacheCountLimit(count);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsExternalKt_Graphics_1nGetFontCacheCountUsed
  (JNIEnv* env, jclass jclass) {
  return SkGraphics::GetFontCacheCountUsed();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsExternalKt_Graphics_1nGetResourceCacheTotalByteLimit
  (JNIEnv* env, jclass jclass) {
  return SkGraphics::GetResourceCacheTotalByteLimit();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsExternalKt_Graphics_1nSetResourceCacheTotalByteLimit
  (JNIEnv* env, jclass jclass, jint bytes) {
  return SkGraphics::SetResourceCacheTotalByteLimit(bytes);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsExternalKt_Graphics_1nGetResourceCacheSingleAllocationByteLimit
  (JNIEnv* env, jclass jclass) {
  return SkGraphics::GetResourceCacheSingleAllocationByteLimit();
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsExternalKt_Graphics_1nSetResourceCacheSingleAllocationByteLimit
  (JNIEnv* env, jclass jclass, jint bytes) {
  return SkGraphics::SetResourceCacheSingleAllocationByteLimit(bytes);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_GraphicsExternalKt_Graphics_1nGetResourceCacheTotalBytesUsed
  (JNIEnv* env, jclass jclass) {
  return SkGraphics::GetResourceCacheTotalBytesUsed();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_GraphicsExternalKt_Graphics_1nPurgeFontCache
  (JNIEnv* env, jclass jclass) {
  SkGraphics::PurgeFontCache();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_GraphicsExternalKt_Graphics_1nPurgeResourceCache
  (JNIEnv* env, jclass jclass) {
  SkGraphics::PurgeResourceCache();
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_GraphicsExternalKt_Graphics_1nPurgeAllCaches
  (JNIEnv* env, jclass jclass) {
  SkGraphics::PurgeAllCaches();
}
