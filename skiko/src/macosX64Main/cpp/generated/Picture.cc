
// This file has been auto generated.

#include <iostream>
#include "SkData.h"
#include "SkPicture.h"
#include "SkShader.h"
#include "common.h"

extern "C" jlong org_jetbrains_skia_Picture__1nMakeFromData
  (kref __Kinstance, jlong dataPtr) {
    SkData* data = reinterpret_cast<SkData*>(static_cast<uintptr_t>(dataPtr));
    SkPicture* instance = SkPicture::MakeFromData(data).release();
    return reinterpret_cast<jlong>(instance);
}


extern "C" void org_jetbrains_skia_Picture__1nPlayback
  (kref __Kinstance, jlong ptr, jlong canvasPtr, jobject abort) {
    TODO("implement org_jetbrains_skia_Picture__1nPlayback");
}
     
#if 0 
extern "C" void org_jetbrains_skia_Picture__1nPlayback
  (kref __Kinstance, jlong ptr, jlong canvasPtr, jobject abort) {
    SkPicture* instance = reinterpret_cast<SkPicture*>(static_cast<uintptr_t>(ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    if (abort == nullptr) {
        instance->playback(canvas, nullptr);
    } else {
        BooleanSupplierAbort callback(env, abort);
        instance->playback(canvas, &callback);
    }
}
#endif



extern "C" jobject org_jetbrains_skia_Picture__1nGetCullRect
  (kref __Kinstance, jlong ptr) {
    TODO("implement org_jetbrains_skia_Picture__1nGetCullRect");
}
     
#if 0 
extern "C" jobject org_jetbrains_skia_Picture__1nGetCullRect
  (kref __Kinstance, jlong ptr) {
    SkPicture* instance = reinterpret_cast<SkPicture*>(static_cast<uintptr_t>(ptr));
    return skija::Rect::fromSkRect(env, instance->cullRect());
}
#endif


extern "C" jint org_jetbrains_skia_Picture__1nGetUniqueId
  (kref __Kinstance, jlong ptr) {
    SkPicture* instance = reinterpret_cast<SkPicture*>(static_cast<uintptr_t>(ptr));
    return instance->uniqueID();
}

extern "C" jlong org_jetbrains_skia_Picture__1nSerializeToData
  (kref __Kinstance, jlong ptr) {
    SkPicture* instance = reinterpret_cast<SkPicture*>(static_cast<uintptr_t>(ptr));
    SkData* data = instance->serialize().release();
    return reinterpret_cast<jlong>(data);
}

extern "C" jlong org_jetbrains_skia_Picture__1nMakePlaceholder
  (kref __Kinstance, jfloat left, jfloat top, jfloat right, jfloat bottom) {
    SkRect cull = SkRect::MakeLTRB(left, top, right, bottom);
    SkPicture* instance = SkPicture::MakePlaceholder(cull).release();
    return reinterpret_cast<jlong>(instance);
}

extern "C" jint org_jetbrains_skia_Picture__1nGetApproximateOpCount
  (kref __Kinstance, jlong ptr) {
    SkPicture* instance = reinterpret_cast<SkPicture*>(static_cast<uintptr_t>(ptr));
    return instance->approximateOpCount();
}

extern "C" jlong org_jetbrains_skia_Picture__1nGetApproximateBytesUsed
  (kref __Kinstance, jlong ptr) {
    SkPicture* instance = reinterpret_cast<SkPicture*>(static_cast<uintptr_t>(ptr));
    return instance->approximateBytesUsed();
}


extern "C" jlong org_jetbrains_skia_Picture__1nMakeShader
  (kref __Kinstance, jlong ptr, jint tmxValue, jint tmyValue, jint filterModeValue, jfloatArray localMatrixArr, jobject tileRectObj) {
    TODO("implement org_jetbrains_skia_Picture__1nMakeShader");
}
     
#if 0 
extern "C" jlong org_jetbrains_skia_Picture__1nMakeShader
  (kref __Kinstance, jlong ptr, jint tmxValue, jint tmyValue, jint filterModeValue, jfloatArray localMatrixArr, jobject tileRectObj) {
    SkPicture* instance = reinterpret_cast<SkPicture*>(static_cast<uintptr_t>(ptr));
    SkTileMode tmx = static_cast<SkTileMode>(tmxValue);
    SkTileMode tmy = static_cast<SkTileMode>(tmyValue);
    SkFilterMode filterMode = static_cast<SkFilterMode>(filterModeValue);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, localMatrixArr);
    std::unique_ptr<SkRect> tileRect = skija::Rect::toSkRect(env, tileRectObj);
    SkShader* shader = instance->makeShader(tmx, tmy, filterMode, localMatrix.get(), tileRect.get()).release();
    return reinterpret_cast<jlong>(shader);
}
#endif

