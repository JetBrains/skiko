
// This file has been auto generated.

#include <iostream>
#include "SkData.h"
#include "SkPicture.h"
#include "SkShader.h"
#include "common.h"


#ifndef __EMSCRIPTEN__

typedef bool((*InteropCallback)(const void*));
typedef void((*InteropDisposeCallback)(const void*));

class KotlinAbortCallback: public SkPicture::AbortCallback {
public:
    KotlinAbortCallback(InteropCallback cb, InteropDisposeCallback drop, void* data) : cb(cb), drop(drop), data(data) {}

    virtual ~KotlinAbortCallback() override {
        drop(data);
    }

    KotlinAbortCallback(const KotlinAbortCallback&) = delete;
    KotlinAbortCallback(KotlinAbortCallback&&) = delete;
    KotlinAbortCallback& operator=(const KotlinAbortCallback&) = delete;
    KotlinAbortCallback& operator=(KotlinAbortCallback&&) = delete;

    bool abort() override {
        return cb(data);
    }
private:
    void * data;
    InteropCallback cb;
    InteropDisposeCallback drop;
};

#else // __EMSCRIPTEN__

typedef KInt InteropCallback;

class KotlinAbortCallback: public SkPicture::AbortCallback {
public:
    KotlinAbortCallback(InteropCallback cb) : cb(cb) {}
    virtual ~KotlinAbortCallback() override {
        EM_ASM({ _releaseCallback($0) }, cb);
    }

    KotlinAbortCallback(const KotlinAbortCallback&) = delete;
    KotlinAbortCallback(KotlinAbortCallback&&) = delete;
    KotlinAbortCallback& operator=(const KotlinAbortCallback&) = delete;
    KotlinAbortCallback& operator=(KotlinAbortCallback&&) = delete;

    bool abort() override {
        int value = EM_ASM_INT({
            return _callCallback($0).value ? 1 : 0;
        }, cb);
        return value == 1;
    }
private:
    InteropCallback cb;
};

#endif // __EMSCRIPTEN__

SKIKO_EXPORT KNativePointer org_jetbrains_skia_Picture__1nMakeFromData
  (KNativePointer dataPtr) {
    SkData* data = reinterpret_cast<SkData*>((dataPtr));
    SkPicture* instance = SkPicture::MakeFromData(data).release();
    return reinterpret_cast<KNativePointer>(instance);
}

#ifndef __EMSCRIPTEN__

SKIKO_EXPORT void org_jetbrains_skia_Picture__1nPlayback
  (KNativePointer ptr, KNativePointer canvasPtr, InteropCallback abort, InteropDisposeCallback drop, void* data) {
    SkPicture* instance = reinterpret_cast<SkPicture*>((ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    if (data == nullptr) {
        instance->playback(canvas, nullptr);
    } else {
        KotlinAbortCallback abortCallback(abort, drop, data);
        instance->playback(canvas, &abortCallback);
    }
}

#else // __EMSCRIPTEN__

SKIKO_EXPORT void org_jetbrains_skia_Picture__1nPlayback
  (KNativePointer ptr, KNativePointer canvasPtr, InteropCallback abort) {
    SkPicture* instance = reinterpret_cast<SkPicture*>((ptr));
    SkCanvas* canvas = reinterpret_cast<SkCanvas*>((canvasPtr));
    if (abort == 0) {
        instance->playback(canvas, nullptr);
    } else {
        KotlinAbortCallback abortCallback(abort);
        instance->playback(canvas, &abortCallback);
    }
}

#endif // __EMSCRIPTEN__


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
  (KNativePointer ptr, KInt tmxValue, KInt tmyValue, KInt filterModeValue, KFloat* localMatrixArr, KInteropPointer tileRectLTRB) {
    SkPicture* instance = reinterpret_cast<SkPicture*>((ptr));
    SkTileMode tmx = static_cast<SkTileMode>(tmxValue);
    SkTileMode tmy = static_cast<SkTileMode>(tmyValue);
    SkFilterMode filterMode = static_cast<SkFilterMode>(filterModeValue);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(localMatrixArr);
    float* ltrb = reinterpret_cast<float*>(tileRectLTRB);
    SkShader* shader;
    if (ltrb) {
        SkRect tileRect = SkRect::MakeLTRB(ltrb[0], ltrb[1], ltrb[2], ltrb[3]);
        shader = instance->makeShader(tmx, tmy, filterMode, localMatrix.get(), &tileRect).release();
    } else {
        shader = instance->makeShader(tmx, tmy, filterMode, localMatrix.get(), nullptr).release();
    }
    return reinterpret_cast<KNativePointer>(shader);
}

