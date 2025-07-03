#include <iostream>
#include <jni.h>
#include "interop.hh"
#include "SkData.h"
#include "SkPicture.h"
#include "SkShader.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PictureExternalKt_Picture_1nMakeFromData
  (JNIEnv* env, jclass jclass, jlong dataPtr) {
    SkData* data = reinterpret_cast<SkData*>(static_cast<uintptr_t>(dataPtr));
    SkPicture* instance = SkPicture::MakeFromData(data).release();
    return reinterpret_cast<jlong>(instance);
}

class JAbortCallback: public SkPicture::AbortCallback {
public:
    JAbortCallback(JNIEnv* env, jobject supplier) : callback(env, supplier) {}

    bool abort() override {
        bool res = static_cast<bool>(callback());
        if (callback.isExceptionThrown())
          return false;
        return res;
    }
private:
    JBooleanCallback callback;
};

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PictureExternalKt_Picture_1nPlayback
  (JNIEnv* env, jclass jclass, jlong ptr, jlong canvasPtr, jobject abort) {
    SkPicture* instance = reinterpret_cast<SkPicture*>(static_cast<uintptr_t>(ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>(static_cast<uintptr_t>(canvasPtr));
    if (abort == nullptr) {
        instance->playback(canvas, nullptr);
    } else {
        JAbortCallback callback(env, abort);
        instance->playback(canvas, &callback);
    }
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_PictureExternalKt_Picture_1nGetCullRect
  (JNIEnv* env, jclass jclass, jlong ptr, jfloatArray ltrbArray) {
    SkPicture* instance = reinterpret_cast<SkPicture*>(static_cast<uintptr_t>(ptr));
    SkRect cullRect = instance->cullRect();
    env->SetFloatArrayRegion(ltrbArray, 0, 4, reinterpret_cast<const jfloat*>(cullRect.asScalars()));
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_PictureExternalKt_Picture_1nGetUniqueId
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPicture* instance = reinterpret_cast<SkPicture*>(static_cast<uintptr_t>(ptr));
    return instance->uniqueID();
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PictureExternalKt_Picture_1nSerializeToData
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPicture* instance = reinterpret_cast<SkPicture*>(static_cast<uintptr_t>(ptr));
    SkData* data = instance->serialize().release();
    return reinterpret_cast<jlong>(data);
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PictureExternalKt_Picture_1nMakePlaceholder
  (JNIEnv* env, jclass jclass, jfloat left, jfloat top, jfloat right, jfloat bottom) {
    SkRect cull = SkRect::MakeLTRB(left, top, right, bottom);
    SkPicture* instance = SkPicture::MakePlaceholder(cull).release();
    return reinterpret_cast<jlong>(instance);
}

extern "C" JNIEXPORT jint JNICALL Java_org_jetbrains_skia_PictureExternalKt_Picture_1nGetApproximateOpCount
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPicture* instance = reinterpret_cast<SkPicture*>(static_cast<uintptr_t>(ptr));
    return instance->approximateOpCount();
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PictureExternalKt_Picture_1nGetApproximateBytesUsed
  (JNIEnv* env, jclass jclass, jlong ptr) {
    SkPicture* instance = reinterpret_cast<SkPicture*>(static_cast<uintptr_t>(ptr));
    return instance->approximateBytesUsed();
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_PictureExternalKt_Picture_1nMakeShader
  (JNIEnv* env, jclass jclass, jlong ptr, jint tmxValue, jint tmyValue, jint filterModeValue, jfloatArray localMatrixArr, jboolean hasTile, jfloat tileLeft, jfloat tileTop, jfloat tileRight, jfloat tileBottom) {
    SkPicture* instance = reinterpret_cast<SkPicture*>(static_cast<uintptr_t>(ptr));
    SkTileMode tmx = static_cast<SkTileMode>(tmxValue);
    SkTileMode tmy = static_cast<SkTileMode>(tmyValue);
    SkFilterMode filterMode = static_cast<SkFilterMode>(filterModeValue);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, localMatrixArr);
    SkShader* shader;
    if (hasTile) {
        SkRect tileRect = SkRect::MakeLTRB(tileLeft, tileRight, tileBottom, tileTop);
        shader = instance->makeShader(tmx, tmy, filterMode, localMatrix.get(), &tileRect).release();
    } else {
        shader = instance->makeShader(tmx, tmy, filterMode, localMatrix.get(), nullptr).release();
    }
    return reinterpret_cast<jlong>(shader);
}