
// This file has been auto generated.

#include "SkPixelRef.h"
#include "common.h"

extern "C" jint org_jetbrains_skia_PixelRef__1nGetWidth
  (kref __Kinstance, jlong ptr) {
    SkPixelRef* instance = reinterpret_cast<SkPixelRef*>(static_cast<uintptr_t>(ptr));
    return instance->width();
}

extern "C" jint org_jetbrains_skia_PixelRef__1nGetHeight
  (kref __Kinstance, jlong ptr) {
    SkPixelRef* instance = reinterpret_cast<SkPixelRef*>(static_cast<uintptr_t>(ptr));
    return instance->height();
}

extern "C" jlong org_jetbrains_skia_PixelRef__1nGetRowBytes
  (kref __Kinstance, jlong ptr) {
    SkPixelRef* instance = reinterpret_cast<SkPixelRef*>(static_cast<uintptr_t>(ptr));
    return instance->rowBytes();
}

extern "C" jint org_jetbrains_skia_PixelRef__1nGetGenerationId
  (kref __Kinstance, jlong ptr) {
    SkPixelRef* instance = reinterpret_cast<SkPixelRef*>(static_cast<uintptr_t>(ptr));
    return instance->getGenerationID();
}

extern "C" void org_jetbrains_skia_PixelRef__1nNotifyPixelsChanged
  (kref __Kinstance, jlong ptr) {
    SkPixelRef* instance = reinterpret_cast<SkPixelRef*>(static_cast<uintptr_t>(ptr));
    instance->notifyPixelsChanged();
}

extern "C" jboolean org_jetbrains_skia_PixelRef__1nIsImmutable
  (kref __Kinstance, jlong ptr) {
    SkPixelRef* instance = reinterpret_cast<SkPixelRef*>(static_cast<uintptr_t>(ptr));
    return instance->isImmutable();
}

extern "C" void org_jetbrains_skia_PixelRef__1nSetImmutable
  (kref __Kinstance, jlong ptr) {
    SkPixelRef* instance = reinterpret_cast<SkPixelRef*>(static_cast<uintptr_t>(ptr));
    instance->setImmutable();
}
