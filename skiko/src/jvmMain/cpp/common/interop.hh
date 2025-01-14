#pragma once
#include <iostream>
#include <jni.h>
#include <memory>
#include <vector>
#include "SkCodec.h"
#include "SkFontMetrics.h"
#include "SkFontStyle.h"
#include "SkImageInfo.h"
#include "SkMatrix.h"
#include "SkM44.h"
#include "SkPaint.h"
#include "SkPicture.h"
#include "SkRefCnt.h"
#include "SkRect.h"
#include "SkRRect.h"
#include "SkScalar.h"
#include "SkShaper.h"
#include "SkString.h"
#include "SkSurfaceProps.h"
#include "TextStyle.h"
#include "mppinterop.h"

namespace java {
    namespace io {
        namespace OutputStream {
            extern jmethodID write;
            extern jmethodID flush;
            void onLoad(JNIEnv* env);
        }
    }

    namespace lang {
        namespace Boolean {
            extern jclass cls;
            extern jmethodID booleanValue;
            void onLoad(JNIEnv* env);
            void onUnload(JNIEnv* env);
        }

        namespace Float {
            extern jclass cls;
            extern jmethodID ctor;
            void onLoad(JNIEnv* env);
            void onUnload(JNIEnv* env);
        }

        namespace RuntimeException {
            extern jclass cls;
            void onLoad(JNIEnv* env);
            void onUnload(JNIEnv* env);
        }

        namespace String {
            extern jclass cls;
            void onLoad(JNIEnv* env);
            void onUnload(JNIEnv* env);
        }

        namespace Throwable {
            extern jmethodID printStackTrace;
            void onLoad(JNIEnv* env);
            bool exceptionThrown(JNIEnv* env);
        }

        namespace System {
            extern jclass cls;
            extern jmethodID getProperty;
            void onLoad(JNIEnv* env);
            void onUnload(JNIEnv* env);
        }
    }

    namespace util {
        namespace Iterator {
            extern jclass cls;
            extern jmethodID next;
            extern jmethodID hasNext;
            void onLoad(JNIEnv* env);
            void onUnload(JNIEnv* env);
        }
    }

    void onLoad(JNIEnv* env);
    void onUnload(JNIEnv* env);
}

namespace skija {
    namespace AnimationFrameInfo {
        extern jclass cls;
        extern jmethodID ctor;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
        jobject toJava(JNIEnv* env, const SkCodec::FrameInfo& i);
        void copyToInterop(JNIEnv* env, const SkCodec::FrameInfo& info, jintArray dst);
        void copyToInterop(JNIEnv* env, const std::vector<SkCodec::FrameInfo>& infos, jintArray dst);
    }

    template <typename T>
    class AutoLocal {
    public:
        AutoLocal(JNIEnv* env, T ref): fEnv(env), fRef(ref) {
        }

        AutoLocal(const AutoLocal&) = delete;
        AutoLocal(AutoLocal&&) = default;
        AutoLocal& operator=(AutoLocal const&) = delete;

        ~AutoLocal() {
            if (fRef)
                fEnv->DeleteLocalRef(fRef);
        }

        T get() {
            return fRef;
        }
    private:
        JNIEnv* fEnv;
        T fRef;
    };

    namespace Color4f {
        extern jclass cls;
        extern jmethodID ctor;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
    }

    namespace Drawable {
        extern jclass cls;
        extern jmethodID onDraw;
        extern jmethodID onGetBounds;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
    }

    namespace FontFamilyName {
        extern jclass cls;
        extern jmethodID ctor;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
    }

    namespace FontFeature {
        extern jclass cls;
        extern jmethodID ctor;
        extern jfieldID tag;
        extern jfieldID value;
        extern jfieldID start;
        extern jfieldID end;

        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
        std::vector<SkShaper::Feature> fromJavaArray(JNIEnv* env, jobjectArray featuresArr);

        // every feature is encoded as 4 ints
        std::vector<SkShaper::Feature> fromIntArray(JNIEnv* env, jintArray array, jint featuresCount);

        // caller needs to ensure the resultArr size is sufficient (every feature is encoded as 2 ints)
        void writeToIntArray(std::vector<skia::textlayout::FontFeature> features, int* resultArr);

        namespace FourByteTag {
            int fromString(SkString str);
        }
    }

    namespace FontMetrics {
        extern jclass cls;
        extern jmethodID ctor;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
        jobject toJava(JNIEnv* env, const SkFontMetrics& m);
    }

    namespace FontMgr {
        extern jclass cls;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
    }

    namespace FontStyle {
        SkFontStyle fromJava(jint style);
        jint toJava(const SkFontStyle& fs);
    }

    namespace FontVariation {
        extern jclass cls;
        extern jmethodID ctor;
        extern jfieldID tag;
        extern jfieldID value;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
    }

    namespace FontVariationAxis {
        extern jclass cls;
        extern jmethodID ctor;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
    }

    namespace ImageInfo {
        extern jclass cls;
        extern jmethodID ctor;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
        jobject toJava(JNIEnv* env, const SkImageInfo& imageInfo);
        void writeImageInfoForInterop(JNIEnv* env, SkImageInfo imageInfo, jintArray imageInfoResult, jlongArray colorSpaceResultPtr);
    }

    namespace IPoint {
        extern jclass cls;
        extern jmethodID ctor;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
        jobject make(JNIEnv* env, float x, float y);
        jobject fromSkIPoint(JNIEnv* env, const SkIPoint& p);
    }

    namespace IRect {
        extern jclass cls;
        extern jmethodID makeLTRB;
        extern jfieldID left;
        extern jfieldID top;
        extern jfieldID right;
        extern jfieldID bottom;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
        jobject fromSkIRect(JNIEnv* env, const SkIRect& rect);
        std::unique_ptr<SkIRect> toSkIRect(JNIEnv* env, jintArray obj);
    }

    namespace Path {
        extern jclass cls;
        extern jmethodID ctor;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
    }

    namespace PathSegment {
        extern jclass cls;
        extern jmethodID ctorDone;
        extern jmethodID ctorMoveClose;
        extern jmethodID ctorLine;
        extern jmethodID ctorQuad;
        extern jmethodID ctorConic;
        extern jmethodID ctorCubic;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
    }

    namespace Point {
        extern jclass cls;
        extern jmethodID ctor;
        extern jfieldID x;
        extern jfieldID y;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
        jobject make(JNIEnv* env, float x, float y);
        jobject fromSkPoint(JNIEnv* env, const SkPoint& p);
        jobjectArray fromSkPoints(JNIEnv* env, const std::vector<SkPoint>& ps);

        void copyToInterop(JNIEnv* env, const SkPoint& point, jfloatArray pointer);
    }

    namespace PaintFilterCanvas {
        extern jmethodID onFilterId;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
        bool onFilter(jobject obj, SkPaint& paint);
        jobject attach(JNIEnv* env, jobject obj);
        void detach(jobject obj);
    }

    namespace PictureFilterCanvas {
        extern jmethodID onDrawPictureId;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
        bool onDrawPicture(jobject obj, const SkPicture* picture, const SkMatrix* matrix, const SkPaint* paint);
        jobject attach(JNIEnv* env, jobject obj);
        void detach(jobject obj);
    }

    namespace Rect {
        extern jclass cls;
        extern jmethodID makeLTRB;
        extern jfieldID left;
        extern jfieldID top;
        extern jfieldID right;
        extern jfieldID bottom;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
        std::unique_ptr<SkRect> toSkRect(JNIEnv* env, jobject rect);
        jobject fromLTRB(JNIEnv* env, float left, float top, float right, float bottom);
        jobject fromSkRect(JNIEnv* env, const SkRect& rect);

        void copyToInterop(JNIEnv* env, const SkRect& rect, jfloatArray pointer);
    }

    namespace RRect {
        extern jclass cls;
        extern jmethodID makeLTRB1;
        extern jmethodID makeLTRB2;
        extern jmethodID makeLTRB4;
        extern jmethodID makeNinePatchLTRB;
        extern jmethodID makeComplexLTRB;
        extern jfieldID left;
        extern jfieldID top;
        extern jfieldID right;
        extern jfieldID bottom;
        extern jfieldID radii;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
        SkRRect toSkRRect(JNIEnv* env, jfloat left, jfloat top, jfloat right, jfloat bottom, jfloatArray jradii);
        jobject fromSkRRect(JNIEnv* env, const SkRRect& rect);

        void copyToInterop(JNIEnv* env, const SkRRect& rect, jfloatArray pointer);
    }

    namespace RSXform {
        extern jclass cls;
        extern jmethodID ctor;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
    }

    namespace SurfaceProps {
        std::unique_ptr<SkSurfaceProps> toSkSurfaceProps(JNIEnv* env, jintArray surfacePropsInts);
    }

    namespace SamplingMode {
            SkSamplingOptions unpack(jlong val);
            SkSamplingOptions unpackFrom2Ints(JNIEnv* env, jint val1, jint val2);
        }

    namespace impl {
        namespace Native {
            extern jfieldID _ptr;
            void onLoad(JNIEnv* env);
            void onUnload(JNIEnv* env);
            void* fromJava(JNIEnv* env, jobject obj, jclass cls);
        }
    }

    void onLoad(JNIEnv* env);
    void onUnload(JNIEnv* env);
}

namespace kotlin {
    namespace jvm {
        namespace functions {
            namespace Function0 {
                extern jclass cls;
                extern jmethodID invoke;
                void onLoad(JNIEnv* env);
                void onUnload(JNIEnv* env);
            }
        }
    }

    void onLoad(JNIEnv* env);
    void onUnload(JNIEnv* env);
}

std::unique_ptr<SkMatrix> skMatrix(JNIEnv* env, jfloatArray arr);
std::unique_ptr<SkM44> skM44(JNIEnv* env, jfloatArray arr);

SkString skString(JNIEnv* env, jstring str);
jstring javaString(JNIEnv* env, const SkString& str);
jstring javaString(JNIEnv* env, const char* chars, size_t len);
jstring javaString(JNIEnv* env, const char* chars);

jobject javaFloat(JNIEnv* env, SkScalar val);
jlong packTwoInts(int32_t a, int32_t b);
jlong packIPoint(SkIPoint p);
jlong packISize(SkISize s);

jbyteArray   javaByteArray  (JNIEnv* env, const std::vector<jbyte>& bytes);
jshortArray  javaShortArray (JNIEnv* env, const std::vector<jshort>& shorts);
jintArray    javaIntArray   (JNIEnv* env, const std::vector<jint>& ints);
jlongArray   javaLongArray  (JNIEnv* env, const std::vector<jlong>& longs);
jfloatArray  javaFloatArray (JNIEnv* env, const std::vector<float>& floats);

std::vector<SkString> skStringVector(JNIEnv* env, jobjectArray arr);
jobjectArray javaStringArray(JNIEnv* env, const std::vector<SkString>& strings);

template <typename T>
jlong ptrToJlong(T* ptr) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(ptr));
}

template <typename T>
inline T jlongToPtr(jlong ptr) {
    return reinterpret_cast<T>(static_cast<uintptr_t>(ptr));
}

void deleteJBytes(void* addr, void*);

#ifdef SK_BUILD_FOR_ANDROID
#define SKIKO_JNI_VERSION JNI_VERSION_1_6
#define AS_JNI_ENV_PTR(env) (env)
#else
#define SKIKO_JNI_VERSION JNI_VERSION_1_8
#define AS_JNI_ENV_PTR(env) ((void**)(env))
#endif

static inline jint rawBits(jfloat f) {
    union {
        jfloat f;
        jint i;
    } u;
    u.f = f;
    return u.i;
}

static inline jlong rawBits(jdouble d) {
    union {
        jdouble d;
        jlong l;
    } u;
    u.d = d;
    return u.l;
}

static inline jfloat fromBits(jint i) {
    union {
        jfloat f;
        jint i;
    } u;
    u.i = i;
    return u.f;
}

template<typename T>
T jObjectConvert(JNIEnv* env, jobject obj);

// Callback support

template<typename T>
class JCallback {
public:
    JCallback (JNIEnv* env, jobject callback) : env(env), callback(env->NewGlobalRef(callback)) {
        env->GetJavaVM(&javaVM);
    }

    ~JCallback() {
        if (callback != nullptr) {
            JNIEnv* localEnv;
            if (javaVM->GetEnv(reinterpret_cast<void**>(&localEnv), SKIKO_JNI_VERSION) == JNI_OK) {
                localEnv->DeleteGlobalRef(callback);
            }
        }
    }

    JCallback(const JCallback&) = delete;
    JCallback& operator=(const JCallback&) = delete;

    JCallback(JCallback&& other) noexcept {
        std::swap(env, other.env);
        std::swap(javaVM, other.javaVM);
        std::swap(callback, other.callback);
    }
    JCallback& operator=(JCallback&& other) {
        std::swap(env, other.env);
        std::swap(javaVM, other.javaVM);
        std::swap(callback, other.callback);
        return this;
    }

    T operator()() {
        skija::AutoLocal<jobject> res(env, env->CallObjectMethod(callback, kotlin::jvm::functions::Function0::invoke));
        return jObjectConvert<T>(env, res.get());
    }

    bool isExceptionThrown() {
        return java::lang::Throwable::exceptionThrown(env);
    }
private:
    JNIEnv* env;
    JavaVM* javaVM;
    jobject callback;
};

typedef JCallback<jboolean> JBooleanCallback;
