
// This file has been auto generated.

#include <iostream>
#include "SkData.h"
#include "SkPicture.h"
#include "SkShader.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Picture__1nMakeFromData
  (KNativePointer dataPtr) {
    SkData* data = reinterpret_cast<SkData*>((dataPtr));
    SkPicture* instance = SkPicture::MakeFromData(data).release();
    return reinterpret_cast<KNativePointer>(instance);
}


SKIKO_EXPORT void org_jetbrains_skia_Picture__1nPlayback
  (KNativePointer ptr, KNativePointer canvasPtr, KInteropPointer abort) {
    TODO("implement org_jetbrains_skia_Picture__1nPlayback");
}
     
#if 0 
SKIKO_EXPORT void org_jetbrains_skia_Picture__1nPlayback
  (KNativePointer ptr, KNativePointer canvasPtr, KInteropPointer abort) {
    SkPicture* instance = reinterpret_cast<SkPicture*>((ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    if (abort == nullptr) {
        instance->playback(canvas, nullptr);
    } else {
        BooleanSupplierAbort callback(env, abort);
        instance->playback(canvas, &callback);
    }
}
#endif



SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Picture__1nGetCullRect
  (KNativePointer ptr) {
    TODO("implement org_jetbrains_skia_Picture__1nGetCullRect");
}
     
#if 0 
SKIKO_EXPORT KInteropPointer org_jetbrains_skia_Picture__1nGetCullRect
  (KNativePointer ptr) {
    SkPicture* instance = reinterpret_cast<SkPicture*>((ptr));
    return skija::Rect::fromSkRect(env, instance->cullRect());
}
#endif


SKIKO_EXPORT KInt org_jetbrains_skia_Picture__1nGetUniqueId
  (KNativePointer ptr) {
    SkPicture* instance = reinterpret_cast<SkPicture*>((ptr));
    return instance->uniqueID();
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Picture__1nSerializeToData
  (KNativePointer ptr) {
    SkPicture* instance = reinterpret_cast<SkPicture*>((ptr));
    SkData* data = instance->serialize().release();
    return reinterpret_cast<KNativePointer>(data);
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Picture__1nMakePlaceholder
  (KFloat left, KFloat top, KFloat right, KFloat bottom) {
    SkRect cull = SkRect::MakeLTRB(left, top, right, bottom);
    SkPicture* instance = SkPicture::MakePlaceholder(cull).release();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT KInt org_jetbrains_skia_Picture__1nGetApproximateOpCount
  (KNativePointer ptr) {
    SkPicture* instance = reinterpret_cast<SkPicture*>((ptr));
    return instance->approximateOpCount();
}

SKIKO_EXPORT KLong org_jetbrains_skia_Picture__1nGetApproximateBytesUsed
  (KNativePointer ptr) {
    SkPicture* instance = reinterpret_cast<SkPicture*>((ptr));
    return instance->approximateBytesUsed();
}


SKIKO_EXPORT KNativePointer org_jetbrains_skia_Picture__1nMakeShader
  (KNativePointer ptr, KInt tmxValue, KInt tmyValue, KInt filterModeValue, KFloat* localMatrixArr, KInteropPointer tileRectObj) {
    TODO("implement org_jetbrains_skia_Picture__1nMakeShader");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_Picture__1nMakeShader
  (KNativePointer ptr, KInt tmxValue, KInt tmyValue, KInt filterModeValue, KFloat* localMatrixArr, KInteropPointer tileRectObj) {
    SkPicture* instance = reinterpret_cast<SkPicture*>((ptr));
    SkTileMode tmx = static_cast<SkTileMode>(tmxValue);
    SkTileMode tmy = static_cast<SkTileMode>(tmyValue);
    SkFilterMode filterMode = static_cast<SkFilterMode>(filterModeValue);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, localMatrixArr);
    std::unique_ptr<SkRect> tileRect = skija::Rect::toSkRect(env, tileRectObj);
    SkShader* shader = instance->makeShader(tmx, tmy, filterMode, localMatrix.get(), tileRect.get()).release();
    return reinterpret_cast<KNativePointer>(shader);
}
#endif

