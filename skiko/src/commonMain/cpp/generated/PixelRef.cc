
// This file has been auto generated.

#include "SkPixelRef.h"
#include "common.h"

SKIKO_EXPORT KInt org_jetbrains_skia_PixelRef__1nGetWidth
  (KNativePointer ptr) {
    SkPixelRef* instance = reinterpret_cast<SkPixelRef*>((ptr));
    return instance->width();
}

SKIKO_EXPORT KInt org_jetbrains_skia_PixelRef__1nGetHeight
  (KNativePointer ptr) {
    SkPixelRef* instance = reinterpret_cast<SkPixelRef*>((ptr));
    return instance->height();
}

SKIKO_EXPORT KInt org_jetbrains_skia_PixelRef__1nGetRowBytes
  (KNativePointer ptr) {
    SkPixelRef* instance = reinterpret_cast<SkPixelRef*>((ptr));
    return instance->rowBytes();
}

SKIKO_EXPORT KInt org_jetbrains_skia_PixelRef__1nGetGenerationId
  (KNativePointer ptr) {
    SkPixelRef* instance = reinterpret_cast<SkPixelRef*>((ptr));
    return instance->getGenerationID();
}

SKIKO_EXPORT void org_jetbrains_skia_PixelRef__1nNotifyPixelsChanged
  (KNativePointer ptr) {
    SkPixelRef* instance = reinterpret_cast<SkPixelRef*>((ptr));
    instance->notifyPixelsChanged();
}

SKIKO_EXPORT KBoolean org_jetbrains_skia_PixelRef__1nIsImmutable
  (KNativePointer ptr) {
    SkPixelRef* instance = reinterpret_cast<SkPixelRef*>((ptr));
    return instance->isImmutable();
}

SKIKO_EXPORT void org_jetbrains_skia_PixelRef__1nSetImmutable
  (KNativePointer ptr) {
    SkPixelRef* instance = reinterpret_cast<SkPixelRef*>((ptr));
    instance->setImmutable();
}
