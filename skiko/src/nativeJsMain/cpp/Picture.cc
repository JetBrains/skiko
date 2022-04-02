#include <iostream>
#include "SkData.h"
#include "SkPicture.h"
#include "SkShader.h"
#include "common.h"

class KotlinAbortCallback: public SkPicture::AbortCallback {
public:
    KotlinAbortCallback(KInteropPointer data) : callback(data) {}
    bool abort() override {
        return static_cast<bool>(callback());
    }
private:
    KBooleanCallback callback;
};

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Picture__1nMakeFromData
  (KNativePointer dataPtr) {
    SkData* data = reinterpret_cast<SkData*>((dataPtr));
    SkPicture* instance = SkPicture::MakeFromData(data).release();
    return reinterpret_cast<KNativePointer>(instance);
}

SKIKO_EXPORT void org_jetbrains_skia_Picture__1nPlayback
  (KNativePointer ptr, KNativePointer canvasPtr, KInteropPointer abort) {
    SkPicture* instance = reinterpret_cast<SkPicture*>((ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    if (abort) {
        KotlinAbortCallback abortCallback(abort);
        instance->playback(canvas, &abortCallback);
    } else {
        instance->playback(canvas, nullptr);
    }
}

SKIKO_EXPORT void org_jetbrains_skia_Picture__1nGetCullRect
  (KNativePointer ptr, KInteropPointer ltrbArray) {
    SkPicture* instance = reinterpret_cast<SkPicture*>((ptr));
    SkRect cullRect = instance->cullRect();
    float* ltrb = reinterpret_cast<float*>(ltrbArray);
    ltrb[0] = cullRect.left();
    ltrb[1] = cullRect.top();
    ltrb[2] = cullRect.right();
    ltrb[3] = cullRect.bottom();
}

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
  (KNativePointer ptr, KInt tmxValue, KInt tmyValue, KInt filterModeValue, KFloat* localMatrixArr, KBoolean hasTile, KFloat tileLeft, KFloat tileTop, KFloat tileRight, KFloat tileBottom) {
    SkPicture* instance = reinterpret_cast<SkPicture*>((ptr));
    SkTileMode tmx = static_cast<SkTileMode>(tmxValue);
    SkTileMode tmy = static_cast<SkTileMode>(tmyValue);
    SkFilterMode filterMode = static_cast<SkFilterMode>(filterModeValue);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(localMatrixArr);
    SkShader* shader;
    if (hasTile) {
        SkRect tileRect = SkRect::MakeLTRB(tileLeft, tileRight, tileBottom, tileTop);
        shader = instance->makeShader(tmx, tmy, filterMode, localMatrix.get(), &tileRect).release();
    } else {
        shader = instance->makeShader(tmx, tmy, filterMode, localMatrix.get(), nullptr).release();
    }
    return reinterpret_cast<KNativePointer>(shader);
}

