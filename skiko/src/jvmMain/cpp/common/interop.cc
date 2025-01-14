#include <array>
#include <cstring>
#include "interop.hh"
#include <iostream>
#include <jni.h>
#include <memory>
#include "shaper/interop.hh"
#include "src/base/SkUTF.h"
#include "paragraph/interop.hh"
#include "TextStyle.h"
#include "include/core/SkBlendMode.h"
#include "include/codec/SkCodecAnimation.h"

namespace java {
    namespace io {
        namespace OutputStream {
            jclass cls;
            jmethodID write;
            jmethodID flush;

            void onLoad(JNIEnv* env) {
                jclass cls = env->FindClass("java/io/OutputStream");
                write = env->GetMethodID(cls, "write", "([BII)V");
                flush = env->GetMethodID(cls, "flush", "()V");
            }
        }
    }

    namespace lang {
        namespace Boolean {
            jclass cls;
            jmethodID booleanValue;

            void onLoad(JNIEnv* env) {
                jclass local = env->FindClass("java/lang/Boolean");
                cls  = static_cast<jclass>(env->NewGlobalRef(local));
                booleanValue = env->GetMethodID(cls, "booleanValue", "()Z");
            }

            void onUnload(JNIEnv* env) {
                env->DeleteGlobalRef(cls);
            }
        }

        namespace Float {
            jclass cls;
            jmethodID ctor;

            void onLoad(JNIEnv* env) {
                jclass local = env->FindClass("java/lang/Float");
                cls  = static_cast<jclass>(env->NewGlobalRef(local));
                ctor = env->GetMethodID(cls, "<init>", "(F)V");
            }

            void onUnload(JNIEnv* env) {
                env->DeleteGlobalRef(cls);
            }
        }

        namespace RuntimeException {
            jclass cls;

            void onLoad(JNIEnv* env) {
                jclass local = env->FindClass("java/lang/RuntimeException");
                cls  = static_cast<jclass>(env->NewGlobalRef(local));
            }

            void onUnload(JNIEnv* env) {
                env->DeleteGlobalRef(cls);
            }
        }

        namespace String {
            jclass cls;

            void onLoad(JNIEnv* env) {
                jclass local = env->FindClass("java/lang/String");
                cls = static_cast<jclass>(env->NewGlobalRef(local));
            }

            void onUnload(JNIEnv* env) {
                env->DeleteGlobalRef(cls);
            }
        }

        namespace Throwable {
            jmethodID printStackTrace;

            void onLoad(JNIEnv* env) {
                jclass cls = env->FindClass("java/lang/Throwable");
                printStackTrace = env->GetMethodID(cls, "printStackTrace", "()V");
            }

            bool exceptionThrown(JNIEnv* env) {
                if (env->ExceptionCheck()) {
                    auto th = skija::AutoLocal<jthrowable>(env, env->ExceptionOccurred());
                    env->CallVoidMethod(th.get(), printStackTrace);
                    env->ExceptionCheck(); // ignore
                    return true;
                } else
                    return false;
            }
        }

        namespace System {
            jclass cls;
            jmethodID getProperty;
            void onLoad(JNIEnv* env) {
                jclass local = env->FindClass("java/lang/System");
                cls = static_cast<jclass>(env->NewGlobalRef(local));
                getProperty = env->GetStaticMethodID(cls, "getProperty", "(Ljava/lang/String;)Ljava/lang/String;");
            }
            void onUnload(JNIEnv* env) {
                env->DeleteGlobalRef(cls);
            }
        }
    }

    namespace util {
        namespace Iterator {
            jclass cls;
            jmethodID next;
            jmethodID hasNext;

            void onLoad(JNIEnv* env) {
                jclass local = env->FindClass("java/util/Iterator");
                cls  = static_cast<jclass>(env->NewGlobalRef(local));
                next = env->GetMethodID(cls, "next", "()Ljava/lang/Object;");
                hasNext = env->GetMethodID(cls, "hasNext", "()Z");
            }

            void onUnload(JNIEnv* env) {
                env->DeleteGlobalRef(cls);
            }
        }
    }

    void onLoad(JNIEnv* env) {
        io::OutputStream::onLoad(env);
        lang::Boolean::onLoad(env);
        lang::Float::onLoad(env);
        lang::RuntimeException::onLoad(env);
        lang::String::onLoad(env);
        lang::Throwable::onLoad(env);
        lang::System::onLoad(env);
        util::Iterator::onLoad(env);
    }

    void onUnload(JNIEnv* env) {
        util::Iterator::onUnload(env);
        lang::String::onUnload(env);
        lang::RuntimeException::onUnload(env);
        lang::Float::onUnload(env);
        lang::Boolean::onUnload(env);
        lang::System::onUnload(env);
    }
}

namespace skija {
    namespace AnimationFrameInfo {
        jclass cls;
        jmethodID ctor;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skia/AnimationFrameInfo");
            cls  = static_cast<jclass>(env->NewGlobalRef(local));
            ctor = env->GetMethodID(cls, "<init>", "(IIZIZIILorg/jetbrains/skia/IRect;)V");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }

        jobject toJava(JNIEnv* env, const SkCodec::FrameInfo& i) {
            SkBlendMode blend;
            switch (i.fBlend) {
                case SkCodecAnimation::Blend::kSrcOver:
                    blend = SkBlendMode::kSrcOver;
                    break;
                case SkCodecAnimation::Blend::kSrc:
                    blend = SkBlendMode::kSrc;
                    break;
            }
            jobject res = env->NewObject(cls, ctor,
                                         i.fRequiredFrame,
                                         i.fDuration,
                                         i.fFullyReceived,
                                         static_cast<jint>(i.fAlphaType),
                                         i.fHasAlphaWithinBounds,
                                         static_cast<jint>(i.fDisposalMethod),
                                         static_cast<jint>(blend),
                                         IRect::fromSkIRect(env, i.fFrameRect));
            return java::lang::Throwable::exceptionThrown(env) ? nullptr : res;
        }

        void copyToInteropAtIndex(JNIEnv* env, const SkCodec::FrameInfo& info, jintArray dst, jsize index) {
            jint repr[11] {
                    info.fRequiredFrame,
                    info.fDuration,
                    static_cast<jint>(info.fFullyReceived),
                    static_cast<jint>(info.fAlphaType),
                    static_cast<jint>(info.fHasAlphaWithinBounds),
                    static_cast<jint>(info.fDisposalMethod),
                    static_cast<jint>(info.fBlend),
                    info.fFrameRect.left(),
                    info.fFrameRect.top(),
                    info.fFrameRect.right(),
                    info.fFrameRect.bottom()
            };
            env->SetIntArrayRegion(dst, index * 11, 11, repr);
        }

        void copyToInterop(JNIEnv* env, const SkCodec::FrameInfo& info, jintArray dst) {
            copyToInteropAtIndex(env, info, dst, 0);
        }

        void copyToInterop(JNIEnv* env, const std::vector<SkCodec::FrameInfo>& infos, jintArray dst) {
            jsize i = 0;
            for (const auto& info : infos) {
                copyToInteropAtIndex(env, info, dst, i++);
            }
        }
    }

    namespace Color4f {
        jclass cls;
        jmethodID ctor;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skia/Color4f");
            cls  = static_cast<jclass>(env->NewGlobalRef(local));
            ctor = env->GetMethodID(cls, "<init>", "(FFFF)V");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }
    }

    namespace Drawable {
        jclass cls;
        jmethodID onDraw;
        jmethodID onGetBounds;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skia/Drawable");
            cls = static_cast<jclass>(env->NewGlobalRef(local));
            onDraw = env->GetMethodID(cls, "_onDraw", "(J)V");
            onGetBounds = env->GetMethodID(cls, "onGetBounds", "()Lorg/jetbrains/skia/Rect;");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }
    }

    namespace FontFamilyName {
        jclass cls;
        jmethodID ctor;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skia/FontFamilyName");
            cls  = static_cast<jclass>(env->NewGlobalRef(local));
            ctor = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }
    }

    namespace FontFeature {
        jclass cls;
        jmethodID ctor;
        jfieldID tag;
        jfieldID value;
        jfieldID start;
        jfieldID end;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skia/FontFeature");
            cls  = static_cast<jclass>(env->NewGlobalRef(local));
            ctor = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;I)V");
            tag   = env->GetFieldID(cls, "_tag",   "I");
            value = env->GetFieldID(cls, "value", "I");
            start = env->GetFieldID(cls, "start", "I");
            end   = env->GetFieldID(cls, "end",   "I");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }

        std::vector<SkShaper::Feature> fromJavaArray(JNIEnv* env, jobjectArray featuresArr) {
            jsize featuresLen = featuresArr == nullptr ? 0 : env->GetArrayLength(featuresArr);
            std::vector<SkShaper::Feature> features(featuresLen);
            for (int i = 0; i < featuresLen; ++i) {
                skija::AutoLocal<jobject> featureObj(env, env->GetObjectArrayElement(featuresArr, i));
                features[i] = {static_cast<SkFourByteTag>(env->GetIntField(featureObj.get(), skija::FontFeature::tag)),
                               static_cast<uint32_t>(env->GetIntField(featureObj.get(), skija::FontFeature::value)),
                               static_cast<size_t>(env->GetIntField(featureObj.get(), skija::FontFeature::start)),
                               static_cast<size_t>(env->GetIntField(featureObj.get(), skija::FontFeature::end))};
            }
            return features;
        }

        // every feature is encoded as 4 ints
        std::vector<SkShaper::Feature> fromIntArray(JNIEnv* env, jintArray array, jint featuresLen) {
            jint* ints = env->GetIntArrayElements(array, NULL);
            std::vector<SkShaper::Feature> features(featuresLen);
            for (int i = 0; i < featuresLen; ++i) {
                int j = i * 4;
                features[i] = {
                    static_cast<SkFourByteTag>(ints[j]),
                    static_cast<uint32_t>(ints[j + 1]),
                    static_cast<size_t>(ints[j + 2]),
                    static_cast<size_t>(ints[j + 3])
                };
            }
            env->ReleaseIntArrayElements(array, ints, 0);

            return features;
        }

        // every FontFeature is represented by 2 ints in resultArr
        void writeToIntArray(std::vector<skia::textlayout::FontFeature> features, int* resultArr) {
            for (int i = 0; i < features.size(); ++i) {
                int j = i * 2;
                resultArr[j] = skija::FontFeature::FourByteTag::fromString(features[i].fName);
                resultArr[j + 1] = features[i].fValue;;
            }
        }

        namespace FourByteTag {
            int fromString(SkString str) {
                int code1 = (int)str[0];
                int code2 = (int)str[1];
                int code3 = (int)str[2];
                int code4 = (int)str[3];
                return (code1 & 0xFF << 24) | (code2 & 0xFF << 16) | (code3 & 0xFF << 8) | (code4 & 0xFF);
            }
        }
    }

    namespace FontMetrics {
        jclass cls;
        jmethodID ctor;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skia/FontMetrics");
            cls  = static_cast<jclass>(env->NewGlobalRef(local));
            ctor = env->GetMethodID(cls, "<init>", "(FFFFFFFFFFFLjava/lang/Float;Ljava/lang/Float;Ljava/lang/Float;Ljava/lang/Float;)V");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }

        jobject toJava(JNIEnv* env, const SkFontMetrics& m) {
            float f1, f2, f3, f4;
            return env->NewObject(cls, ctor,
                m.fTop,
                m.fAscent,
                m.fDescent,
                m.fBottom,
                m.fLeading,
                m.fAvgCharWidth,
                m.fMaxCharWidth,
                m.fXMin,
                m.fXMax,
                m.fXHeight,
                m.fCapHeight,
                m.hasUnderlineThickness(&f1) ? javaFloat(env, f1) : nullptr,
                m.hasUnderlinePosition(&f2)  ? javaFloat(env, f2) : nullptr,
                m.hasStrikeoutThickness(&f3) ? javaFloat(env, f3) : nullptr,
                m.hasStrikeoutPosition(&f4)  ? javaFloat(env, f4) : nullptr);
        }
    }

    namespace FontMgr {
        jclass cls;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skia/FontMgr");
            cls  = static_cast<jclass>(env->NewGlobalRef(local));
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }
    }

    namespace FontStyle {
        SkFontStyle fromJava(jint style) {
            return SkFontStyle(style & 0xFFFF, (style >> 16) & 0xFF, static_cast<SkFontStyle::Slant>((style >> 24) & 0xFF));
        }

        jint toJava(const SkFontStyle& fs) {
            return (static_cast<int>(fs.slant()) << 24)| (fs.width() << 16) | fs.weight();
        }
    }

    namespace FontVariation {
        jclass    cls;
        jmethodID ctor;
        jfieldID  tag;
        jfieldID  value;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skia/FontVariation");
            cls   = static_cast<jclass>(env->NewGlobalRef(local));
            ctor = env->GetMethodID(cls, "<init>", "(IF)V");
            tag   = env->GetFieldID(cls, "_tag", "I");
            value = env->GetFieldID(cls, "value", "F");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }
    }

    namespace FontVariationAxis {
        jclass    cls;
        jmethodID ctor;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skia/FontVariationAxis");
            cls  = static_cast<jclass>(env->NewGlobalRef(local));
            ctor = env->GetMethodID(cls, "<init>", "(IFFFZ)V");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }
    }

    namespace ImageInfo {
        jclass cls;
        jmethodID ctor;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skia/ImageInfo");
            cls   = static_cast<jclass>(env->NewGlobalRef(local));
            ctor = env->GetMethodID(cls, "<init>", "(IIIIJ)V");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }

        jobject toJava(JNIEnv* env, const SkImageInfo& info) {
            return env->NewObject(cls, ctor,
                info.width(),
                info.height(),
                static_cast<jint>(info.colorType()),
                static_cast<jint>(info.alphaType()),
                reinterpret_cast<jlong>(info.refColorSpace().release()));
        }

        void writeImageInfoForInterop(JNIEnv* env, SkImageInfo imageInfo, jintArray imageInfoResult, jlongArray colorSpaceResultPtr) {
            jint *result_int = env->GetIntArrayElements(imageInfoResult, NULL);
            result_int[0] = imageInfo.width();
            result_int[1] = imageInfo.height();
            result_int[2] = static_cast<int>(imageInfo.colorType());
            result_int[3] = static_cast<int>(imageInfo.alphaType());
            env->ReleaseIntArrayElements(imageInfoResult, result_int, 0);

            jlong *result_long = env->GetLongArrayElements(colorSpaceResultPtr, NULL);
            result_long[0] = reinterpret_cast<jlong>(imageInfo.refColorSpace().release());
            env->ReleaseLongArrayElements(colorSpaceResultPtr, result_long, 0);
        }
    }

    namespace IPoint {
        jclass    cls;
        jmethodID ctor;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skia/IPoint");
            cls  = static_cast<jclass>(env->NewGlobalRef(local));
            ctor = env->GetMethodID(cls, "<init>", "(II)V");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }

        jobject make(JNIEnv* env, jint x, jint y) {
            return env->NewObject(cls, ctor, x, y);
        }

        jobject fromSkIPoint(JNIEnv* env, const SkIPoint& p) {
            return env->NewObject(cls, ctor, p.fX, p.fY);
        }
    }

    namespace IRect {
        jclass cls;
        jmethodID makeLTRB;
        jfieldID left;
        jfieldID top;
        jfieldID right;
        jfieldID bottom;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skia/IRect");
            cls      = static_cast<jclass>(env->NewGlobalRef(local));
            makeLTRB = env->GetStaticMethodID(cls, "makeLTRB", "(IIII)Lorg/jetbrains/skia/IRect;");
            left     = env->GetFieldID(cls, "left",   "I");
            top      = env->GetFieldID(cls, "top",    "I");
            right    = env->GetFieldID(cls, "right",  "I");
            bottom   = env->GetFieldID(cls, "bottom", "I");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }

        jobject fromSkIRect(JNIEnv* env, const SkIRect& rect) {
            jobject res = env->CallStaticObjectMethod(cls, makeLTRB, rect.fLeft, rect.fTop, rect.fRight, rect.fBottom);
            return java::lang::Throwable::exceptionThrown(env) ? nullptr : res;
        }

        std::unique_ptr<SkIRect> toSkIRect(JNIEnv* env, jintArray rectInts) {
            if (rectInts == nullptr)
                return std::unique_ptr<SkIRect>(nullptr);
            else {
                jint *ints = env->GetIntArrayElements(rectInts, nullptr);
                auto result = std::unique_ptr<SkIRect>(new SkIRect{
                    ints[0], ints[1], ints[2], ints[3]
                });
                env->ReleaseIntArrayElements(rectInts, ints, 0);
                return result;
            }
        }
    }

    namespace Path {
        jclass cls;
        jmethodID ctor;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skia/Path");
            cls          = static_cast<jclass>(env->NewGlobalRef(local));
            ctor         = env->GetMethodID(cls, "<init>", "(J)V");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }
    }

    namespace PathSegment {
        jclass cls;
        jmethodID ctorDone;
        jmethodID ctorMoveClose;
        jmethodID ctorLine;
        jmethodID ctorQuad;
        jmethodID ctorConic;
        jmethodID ctorCubic;

        void onLoad(JNIEnv* env) {
            jclass local  = env->FindClass("org/jetbrains/skia/PathSegment");
            cls           = static_cast<jclass>(env->NewGlobalRef(local));
            ctorDone      = env->GetMethodID(cls, "<init>", "()V");
            ctorMoveClose = env->GetMethodID(cls, "<init>", "(IFFZ)V");
            ctorLine      = env->GetMethodID(cls, "<init>", "(FFFFZZ)V");
            ctorQuad      = env->GetMethodID(cls, "<init>", "(FFFFFFZ)V");
            ctorConic     = env->GetMethodID(cls, "<init>", "(FFFFFFFZ)V");
            ctorCubic     = env->GetMethodID(cls, "<init>", "(FFFFFFFFZ)V");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }
    }

    namespace Point {
        jclass    cls;
        jmethodID ctor;
        jfieldID x;
        jfieldID y;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skia/Point");
            cls  = static_cast<jclass>(env->NewGlobalRef(local));
            ctor = env->GetMethodID(cls, "<init>", "(FF)V");
            x = env->GetFieldID(cls, "x", "F");
            y = env->GetFieldID(cls, "y", "F");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }

        jobject make(JNIEnv* env, float x, float y) {
            return env->NewObject(cls, ctor, x, y);
        }

        jobject fromSkPoint(JNIEnv* env, const SkPoint& p) {
            return env->NewObject(cls, ctor, p.fX, p.fY);
        }

        jobjectArray fromSkPoints(JNIEnv* env, const std::vector<SkPoint>& ps) {
            jobjectArray res = env->NewObjectArray((jsize) ps.size(), cls, nullptr);
            for (int i = 0; i < ps.size(); ++i) {
                skija::AutoLocal<jobject> pointObj(env, fromSkPoint(env, ps[i]));
                env->SetObjectArrayElement(res, i, pointObj.get());
            }
            return res;
        }

        void copyToInterop(JNIEnv* env, const SkPoint& point, jfloatArray pointer) {
            jfloat* xy = pointer == nullptr ? nullptr : env->GetFloatArrayElements(pointer, 0);
            if (xy != nullptr) {
                xy[0] = point.x();
                xy[1] = point.y();
                env->ReleaseFloatArrayElements(pointer, xy, 0);
            }
        }
    }

    namespace PaintFilterCanvas {
        JavaVM* _vm;
        jmethodID onFilterId;

        void onLoad(JNIEnv* env) {
            env->GetJavaVM(&_vm);
            jclass local = env->FindClass("org/jetbrains/skia/PaintFilterCanvas");
            onFilterId = env->GetMethodID(local, "onFilter", "(J)Z");
        }

        void onUnload(JNIEnv* env) {
        }

        bool onFilter(jobject obj, SkPaint& paint) {
            JNIEnv *env;
            _vm->AttachCurrentThread(AS_JNI_ENV_PTR(&env), NULL);
            jboolean result = env->CallBooleanMethod(obj, onFilterId, reinterpret_cast<jlong>(&paint));
            _vm->DetachCurrentThread();
            return result;
        }

        jobject attach(JNIEnv* env, jobject obj) {
            return env->NewGlobalRef(obj);
        }

        void detach(jobject obj) {
            JNIEnv *env;
            _vm->AttachCurrentThread(AS_JNI_ENV_PTR(&env), NULL);
            env->DeleteGlobalRef(obj);
            _vm->DetachCurrentThread();
        }
    }

    namespace PictureFilterCanvas {
        JavaVM* _vm;
        jmethodID onDrawPictureId;

        void onLoad(JNIEnv* env) {
            env->GetJavaVM(&_vm);
            jclass local = env->FindClass("org/jetbrains/skia/PictureFilterCanvas");
            onDrawPictureId = env->GetMethodID(local, "onDrawPicture", "(JJJ)Z");
        }

        void onUnload(JNIEnv* env) {
        }

        bool onDrawPicture(jobject obj, const SkPicture* picture, const SkMatrix* matrix, const SkPaint* paint) {
            JNIEnv *env;
            _vm->AttachCurrentThread(AS_JNI_ENV_PTR(&env), NULL);
            jboolean result = env->CallBooleanMethod(obj, onDrawPictureId, reinterpret_cast<jlong>(picture), reinterpret_cast<jlong>(matrix), reinterpret_cast<jlong>(paint));
            _vm->DetachCurrentThread();
            return result;
        }

        jobject attach(JNIEnv* env, jobject obj) {
            return env->NewGlobalRef(obj);
        }

        void detach(jobject obj) {
            JNIEnv *env;
            _vm->AttachCurrentThread(AS_JNI_ENV_PTR(&env), NULL);
            env->DeleteGlobalRef(obj);
            _vm->DetachCurrentThread();
        }
    }

    namespace Rect {
        jclass cls;
        jmethodID makeLTRB;
        jfieldID left;
        jfieldID top;
        jfieldID right;
        jfieldID bottom;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skia/Rect");
            cls      = static_cast<jclass>(env->NewGlobalRef(local));
            makeLTRB = env->GetStaticMethodID(cls, "makeLTRB", "(FFFF)Lorg/jetbrains/skia/Rect;");
            left     = env->GetFieldID(cls, "left",   "F");
            top      = env->GetFieldID(cls, "top",    "F");
            right    = env->GetFieldID(cls, "right",  "F");
            bottom   = env->GetFieldID(cls, "bottom", "F");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }

        std::unique_ptr<SkRect> toSkRect(JNIEnv* env, jobject rectObj) {
            if (rectObj == nullptr)
                return std::unique_ptr<SkRect>(nullptr);
            else {
                SkRect* rect = new SkRect();
                rect->setLTRB(env->GetFloatField(rectObj, left),
                              env->GetFloatField(rectObj, top),
                              env->GetFloatField(rectObj, right),
                              env->GetFloatField(rectObj, bottom));
                if (java::lang::Throwable::exceptionThrown(env))
                    return std::unique_ptr<SkRect>(nullptr);
                return std::unique_ptr<SkRect>(rect);
            }
        }

        jobject fromLTRB(JNIEnv* env, float left, float top, float right, float bottom) {
            jobject res = env->CallStaticObjectMethod(cls, makeLTRB, left, top, right, bottom);
            return java::lang::Throwable::exceptionThrown(env) ? nullptr : res;
        }

        jobject fromSkRect(JNIEnv* env, const SkRect& rect) {
            return fromLTRB(env, rect.fLeft, rect.fTop, rect.fRight, rect.fBottom);
        }

        void copyToInterop(JNIEnv* env, const SkRect& rect, jfloatArray pointer) {
            jfloat* ltrb = pointer == nullptr ? nullptr : env->GetFloatArrayElements(pointer, 0);
            if (ltrb != nullptr) {
                ltrb[0] = rect.left();
                ltrb[1] = rect.top();
                ltrb[2] = rect.right();
                ltrb[3] = rect.bottom();
                env->ReleaseFloatArrayElements(pointer, ltrb, 0);
            }
        }
    }

    namespace RRect {
        jclass cls;
        jmethodID makeLTRB1;
        jmethodID makeLTRB2;
        jmethodID makeLTRB4;
        jmethodID makeNinePatchLTRB;
        jmethodID makeComplexLTRB;
        jfieldID left;
        jfieldID top;
        jfieldID right;
        jfieldID bottom;
        jfieldID radii;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skia/RRect");
            cls      = static_cast<jclass>(env->NewGlobalRef(local));
            makeLTRB1 = env->GetStaticMethodID(cls, "makeLTRB", "(FFFFF)Lorg/jetbrains/skia/RRect;");
            makeLTRB2 = env->GetStaticMethodID(cls, "makeLTRB", "(FFFFFF)Lorg/jetbrains/skia/RRect;");
            makeLTRB4 = env->GetStaticMethodID(cls, "makeLTRB", "(FFFFFFFF)Lorg/jetbrains/skia/RRect;");
            makeNinePatchLTRB = env->GetStaticMethodID(cls, "makeNinePatchLTRB", "(FFFFFFFF)Lorg/jetbrains/skia/RRect;");
            makeComplexLTRB = env->GetStaticMethodID(cls, "makeComplexLTRB", "(FFFF[F)Lorg/jetbrains/skia/RRect;");
            left     = env->GetFieldID(cls, "left",   "F");
            top      = env->GetFieldID(cls, "top",    "F");
            right    = env->GetFieldID(cls, "right",  "F");
            bottom   = env->GetFieldID(cls, "bottom", "F");
            radii    = env->GetFieldID(cls, "radii",  "[F");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }

        SkRRect toSkRRect(JNIEnv* env, jfloat left, jfloat top, jfloat right, jfloat bottom, jfloatArray jradii) {
            SkRect rect {left, top, right, bottom};
            SkRRect rrect = SkRRect::MakeEmpty();
            jfloat* radii = env->GetFloatArrayElements(jradii, 0);
            switch (env->GetArrayLength(jradii)) {
                case 1:
                    rrect.setRectXY(rect, radii[0], radii[0]);
                    break;
                case 2:
                    rrect.setRectXY(rect, radii[0], radii[1]);
                    break;
                case 4: {
                    SkVector vradii[4] = {{radii[0], radii[0]}, {radii[1], radii[1]}, {radii[2], radii[2]}, {radii[3], radii[3]}};
                    rrect.setRectRadii(rect, vradii);
                    break;
                }
                case 8: {
                    SkVector vradii[4] = {{radii[0], radii[1]}, {radii[2], radii[3]}, {radii[4], radii[5]}, {radii[6], radii[7]}};
                    rrect.setRectRadii(rect, vradii);
                    break;
                }
            }
            env->ReleaseFloatArrayElements(jradii, radii, 0);
            return rrect;
        }

        jobject fromSkRRect(JNIEnv* env, const SkRRect& rr) {
            const SkRect& r = rr.rect();
            switch (rr.getType()) {
                case SkRRect::Type::kEmpty_Type:
                case SkRRect::Type::kRect_Type:
                    return env->CallStaticObjectMethod(cls, makeLTRB1, r.fLeft, r.fTop, r.fRight, r.fBottom, 0);

                case SkRRect::Type::kOval_Type:
                case SkRRect::Type::kSimple_Type: {
                    float rx = rr.getSimpleRadii().fX;
                    float ry = rr.getSimpleRadii().fY;
                    if (SkScalarNearlyEqual(rx, ry))
                        return env->CallStaticObjectMethod(cls, makeLTRB1, r.fLeft, r.fTop, r.fRight, r.fBottom, rx);
                    else
                        return env->CallStaticObjectMethod(cls, makeLTRB2, r.fLeft, r.fTop, r.fRight, r.fBottom, rx, ry);
                }

                case SkRRect::Type::kNinePatch_Type:
                    return env->CallStaticObjectMethod(cls, makeNinePatchLTRB,
                        r.fLeft, r.fTop, r.fRight, r.fBottom,
                        rr.radii(SkRRect::Corner::kUpperLeft_Corner).fX,
                        rr.radii(SkRRect::Corner::kUpperLeft_Corner).fY,
                        rr.radii(SkRRect::Corner::kLowerRight_Corner).fX,
                        rr.radii(SkRRect::Corner::kLowerRight_Corner).fY);

                case SkRRect::Type::kComplex_Type:
                    std::vector<float> radii = {
                        rr.radii(SkRRect::Corner::kUpperLeft_Corner).fX,
                        rr.radii(SkRRect::Corner::kUpperLeft_Corner).fY,
                        rr.radii(SkRRect::Corner::kUpperRight_Corner).fX,
                        rr.radii(SkRRect::Corner::kUpperRight_Corner).fY,
                        rr.radii(SkRRect::Corner::kLowerRight_Corner).fX,
                        rr.radii(SkRRect::Corner::kLowerRight_Corner).fY,
                        rr.radii(SkRRect::Corner::kLowerLeft_Corner).fX,
                        rr.radii(SkRRect::Corner::kLowerLeft_Corner).fY
                    };

                    return env->CallStaticObjectMethod(cls, makeComplexLTRB, r.fLeft, r.fTop, r.fRight, r.fBottom, javaFloatArray(env, radii));
            }

            return nullptr;
        }

        void copyToInterop(JNIEnv* env, const SkRRect& rect, jfloatArray pointer) {
            jfloat* ltrb = pointer == nullptr ? nullptr : env->GetFloatArrayElements(pointer, 0);
            if (ltrb != nullptr) {
                ltrb[0] = rect.rect().left();
                ltrb[1] = rect.rect().top();
                ltrb[2] = rect.rect().right();
                ltrb[3] = rect.rect().bottom();

                ltrb[4] = rect.radii(SkRRect::kUpperLeft_Corner).x();
                ltrb[5] = rect.radii(SkRRect::kUpperLeft_Corner).y();
                ltrb[6] = rect.radii(SkRRect::kUpperRight_Corner).x();
                ltrb[7] = rect.radii(SkRRect::kUpperRight_Corner).y();
                ltrb[8] = rect.radii(SkRRect::kLowerRight_Corner).x();
                ltrb[9] = rect.radii(SkRRect::kLowerRight_Corner).y();
                ltrb[10] = rect.radii(SkRRect::kLowerLeft_Corner).x();
                ltrb[11] = rect.radii(SkRRect::kLowerLeft_Corner).y();

                env->ReleaseFloatArrayElements(pointer, ltrb, 0);
            }
        }
    }

   namespace RSXform {
        jclass cls;
        jmethodID ctor;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skia/RSXform");
            cls  = static_cast<jclass>(env->NewGlobalRef(local));
            ctor = env->GetMethodID(cls, "<init>", "(FFFF)V");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }
    }

    namespace SurfaceProps {
        std::unique_ptr<SkSurfaceProps> toSkSurfaceProps(JNIEnv* env, jintArray surfacePropsInts) {
            if (surfacePropsInts == nullptr) {
                return std::unique_ptr<SkSurfaceProps>(nullptr);
            }
            jint *ints = env->GetIntArrayElements(surfacePropsInts, NULL);
            uint32_t flags = ints[0];
            SkPixelGeometry geom = static_cast<SkPixelGeometry>(ints[1]);
            env->ReleaseIntArrayElements(surfacePropsInts, ints, 0);
            return std::make_unique<SkSurfaceProps>(flags, geom);
        }
    }

    namespace impl {
        namespace Native {
            jfieldID _ptr;

            void onLoad(JNIEnv* env) {
                jclass cls = env->FindClass("org/jetbrains/skia/impl/Native");
                _ptr = env->GetFieldID(cls, "_ptr", "J");
            }

            void* fromJava(JNIEnv* env, jobject obj, jclass cls) {
                if (env->IsInstanceOf(obj, cls)) {
                    jlong ptr = env->GetLongField(obj, skija::impl::Native::_ptr);
                    return reinterpret_cast<void*>(static_cast<uintptr_t>(ptr));
                }
                return nullptr;
            }
        }
    }

    void onLoad(JNIEnv* env) {
        AnimationFrameInfo::onLoad(env);
        Color4f::onLoad(env);
        Drawable::onLoad(env);
        FontFamilyName::onLoad(env);
        FontFeature::onLoad(env);
        FontMetrics::onLoad(env);
        FontVariation::onLoad(env);
        FontVariationAxis::onLoad(env);
        ImageInfo::onLoad(env);
        IPoint::onLoad(env);
        IRect::onLoad(env);
        Path::onLoad(env);
        PathSegment::onLoad(env);
        Point::onLoad(env);
        PaintFilterCanvas::onLoad(env);
        PictureFilterCanvas::onLoad(env);
        Rect::onLoad(env);
        RRect::onLoad(env);
        RSXform::onLoad(env);

        impl::Native::onLoad(env);
    }

    void onUnload(JNIEnv* env) {
        RSXform::onUnload(env);
        RRect::onUnload(env);
        Rect::onUnload(env);
        PaintFilterCanvas::onUnload(env);
        PictureFilterCanvas::onUnload(env);
        Point::onUnload(env);
        PathSegment::onUnload(env);
        Path::onUnload(env);
        IRect::onUnload(env);
        IPoint::onUnload(env);
        ImageInfo::onUnload(env);
        FontVariationAxis::onUnload(env);
        FontVariation::onUnload(env);
        FontMetrics::onUnload(env);
        FontFeature::onUnload(env);
        FontFamilyName::onUnload(env);
        Drawable::onUnload(env);
        Color4f::onUnload(env);
        AnimationFrameInfo::onUnload(env);
    }
}
std::unique_ptr<SkMatrix> skMatrix(JNIEnv* env, jfloatArray matrixArray) {
    if (matrixArray == nullptr)
        return std::unique_ptr<SkMatrix>(nullptr);
    else {
        jfloat* m = env->GetFloatArrayElements(matrixArray, 0);
        SkMatrix* ptr = new SkMatrix();
        ptr->setAll(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8]);
        env->ReleaseFloatArrayElements(matrixArray, m, 0);
        return std::unique_ptr<SkMatrix>(ptr);
    }
}

std::unique_ptr<SkM44> skM44(JNIEnv* env, jfloatArray matrixArray) {
    if (matrixArray == nullptr)
        return std::unique_ptr<SkM44>(nullptr);
    else {
        jfloat* m = env->GetFloatArrayElements(matrixArray, 0);
        SkM44* ptr = new SkM44(m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8], m[9], m[10], m[11], m[12], m[13], m[14], m[15]);
        env->ReleaseFloatArrayElements(matrixArray, m, 0);
        return std::unique_ptr<SkM44>(ptr);
    }
}

static constexpr inline bool utf16_is_surrogate(uint16_t c) { return (c - 0xD800U) < 2048U; }
static constexpr inline bool utf16_is_high_surrogate(uint16_t c) { return (c & 0xFC00) == 0xD800; }
static constexpr inline bool utf16_is_low_surrogate(uint16_t c) { return (c & 0xFC00) == 0xDC00; }

// U+FFFD REPLACEMENT CHARACTER used to replace an unknown, unrecognized, or unrepresentable character.
#define REPLACEMENT_CHARACTER 0xFFFD

// Replacement of SkUTF::NextUTF16 to carefully handle invalid unicode characters.
// Given a sequence of aligned UTF-16 characters in machine-endian form, return the first unicode codepoint.
// The pointer will be incremented to point at the next codepoint's start.
// If invalid UTF-16 is encountered, return U+FFFD REPLACEMENT_CHARACTER.
static SkUnichar NextUTF16(const uint16_t** ptr, const uint16_t* end) {
    const uint16_t* src = *ptr;
    if (!src || src + 1 > end) {
        return -1;
    }
    uint16_t c = *src++;
    SkUnichar result = REPLACEMENT_CHARACTER;
    if (!utf16_is_surrogate(c)) { // It's single code point, use it as-is
        result = c;
    } else if (utf16_is_high_surrogate(c) && src < end) { // If it's valid start of surrogate range (high surrogate)
        uint16_t low = *src++;
        if (utf16_is_low_surrogate(low)) { // If it's valid end of surrogate range (low surrogate)
            result = (c << 10) + low - 0x35FDC00; // Combine high and low surrogates
        }
    }
    *ptr = src;
    return result;
}

// Replacement of SkUTF::UTF16ToUTF8 to carefully handle invalid unicode strings.
// The only difference here is calling of replaced NextUTF16 function.
static int UTF16ToUTF8(char dst[], int dstCapacity, const uint16_t src[], size_t srcLength) {
    if (!dst) {
        dstCapacity = 0;
    }

    int dstLength = 0;
    const char* endDst = dst + dstCapacity;
    const uint16_t* endSrc = src + srcLength;
    while (src < endSrc) {
        SkUnichar uni = NextUTF16(&src, endSrc);
        if (uni < 0) {
            return -1;
        }

        char utf8[SkUTF::kMaxBytesInUTF8Sequence];
        size_t count = SkUTF::ToUTF8(uni, utf8);
        if (count == 0) {
            return -1;
        }
        dstLength += count;

        if (dst) {
            const char* elems = utf8;
            while (dst < endDst && count > 0) {
                *dst++ = *elems++;
                count -= 1;
            }
        }
    }
    return dstLength;
}

SkString skString(JNIEnv* env, jstring str) {
    if (str == nullptr) {
        return SkString();
    } else {
        // Avoid usage GetStringUTF* functions since they use modified UTF-8.
        // See https://docs.oracle.com/javase/1.5.0/docs/guide/jni/spec/types.html#wp16542
        // Instead, get data as-is (UTF-16) and convert to UTF-8 ourselves.
        jsize utf16Units = env->GetStringLength(str);
        jboolean isCopy;
        const jchar *utf16 = env->GetStringChars(str, &isCopy);

        // SkUTF::UTF16ToUTF8 returns empty string if there is invalid unicode characters.
        // Use our replacement that carefully handles invalid unicode strings.
        int utf8Units = UTF16ToUTF8(nullptr, 0, utf16, utf16Units);
        SkString result;
        if (utf8Units > 0) {
            result.resize(utf8Units);
            UTF16ToUTF8(result.data(), utf8Units, utf16, utf16Units);
        }
        if (isCopy == JNI_TRUE) {
            env->ReleaseStringChars(str, utf16);
        }
        return result;
    }
}

jstring javaString(JNIEnv* env, const SkString& str) {
    return javaString(env, str.c_str(), str.size());
}

jstring javaString(JNIEnv* env, const char* chars, size_t len) {
    if (!chars || !len)
        return nullptr;
    int utf16Units = SkUTF::UTF8ToUTF16(nullptr, 0, chars, len);
    auto utf16 = std::unique_ptr<uint16_t[]>(new uint16_t[utf16Units]);
    SkUTF::UTF8ToUTF16(utf16.get(), utf16Units, chars, len);
    return env->NewString(utf16.get(), utf16Units);
}

jstring javaString(JNIEnv* env, const char* chars) {
    return chars ? javaString(env, chars, strlen(chars)) : nullptr;
}

jobject javaFloat(JNIEnv* env, SkScalar val) {
    return env->NewObject(java::lang::Float::cls, java::lang::Float::ctor, val);
}

jbyteArray javaByteArray(JNIEnv* env, const std::vector<jbyte>& bytes) {
    jbyteArray res = env->NewByteArray((jsize) bytes.size());
    env->SetByteArrayRegion(res, 0, (jsize) bytes.size(), bytes.data());
    return res;
}

jshortArray javaShortArray(JNIEnv* env, const std::vector<jshort>& shorts) {
    jshortArray res = env->NewShortArray((jsize) shorts.size());
    env->SetShortArrayRegion(res, 0, (jsize) shorts.size(), shorts.data());
    return res;
}

jintArray javaIntArray(JNIEnv* env, const std::vector<jint>& ints) {
    jintArray res = env->NewIntArray((jsize) ints.size());
    env->SetIntArrayRegion( res, 0, (jsize) ints.size(), ints.data());
    return res;
}

jlongArray javaLongArray(JNIEnv* env, const std::vector<jlong>& longs) {
    jlongArray res = env->NewLongArray((jsize) longs.size());
    env->SetLongArrayRegion(res, 0, (jsize) longs.size(), longs.data());
    return res;
}

jfloatArray javaFloatArray(JNIEnv* env, const std::vector<jfloat>& floats) {
    jfloatArray res = env->NewFloatArray((jsize) floats.size());
    env->SetFloatArrayRegion(res, 0, (jsize) floats.size(), floats.data());
    return res;
}

std::vector<SkString> skStringVector(JNIEnv* env, jobjectArray arr) {
    if (arr == nullptr) {
        return std::vector<SkString>(0);
    } else {
        jsize len = env->GetArrayLength(arr);
        std::vector<SkString> res(len);
        for (jint i = 0; i < len; ++i) {
            jstring str = static_cast<jstring>(env->GetObjectArrayElement(arr, i));
            res[i] = skString(env, str);
            env->DeleteLocalRef(str);
        }
        return res;
    }
}

jobjectArray javaStringArray(JNIEnv* env, const std::vector<SkString>& strings) {
    jobjectArray res = env->NewObjectArray((jsize) strings.size(), java::lang::String::cls, nullptr);
    for (jint i = 0; i < (jsize) strings.size(); ++i) {
        skija::AutoLocal<jstring> str(env, javaString(env, strings[i]));
        env->SetObjectArrayElement(res, i, str.get());
    }
    return res;
}

void deleteJBytes(void* addr, void*) {
    delete[] (jbyte*) addr;
}

jlong packTwoInts(int32_t a, int32_t b) {
    return (uint64_t (a) << 32) | b;
}

jlong packIPoint(SkIPoint p) {
    return packTwoInts(p.fX, p.fY);
}

jlong packISize(SkISize p) {
    return packTwoInts(p.fWidth, p.fHeight);
}

namespace skija {

    namespace SamplingMode {
        SkSamplingOptions unpack(jlong val) {
            if (0x8000000000000000 & val) {
                val = val & 0x7FFFFFFFFFFFFFFF;
                float* ptr = reinterpret_cast<float*>(&val);
                return SkSamplingOptions(SkCubicResampler {ptr[1], ptr[0]});
            } else {
                int32_t filter = (int32_t) ((val >> 32) & 0xFFFFFFFF);
                int32_t mipmap = (int32_t) (val & 0xFFFFFFFF);
                return SkSamplingOptions(static_cast<SkFilterMode>(filter), static_cast<SkMipmapMode>(mipmap));
            }
        }

        SkSamplingOptions unpackFrom2Ints(JNIEnv* env, jint samplingModeVal1, jint samplingModeVal2) {
            if (0x80000000 & samplingModeVal1) {
                uint64_t val1 = samplingModeVal1 & 0x7FFFFFFF;
                uint64_t val = (val1 << 32) | samplingModeVal2;

                float* ptr = reinterpret_cast<float*>(&val);
                return SkSamplingOptions(SkCubicResampler {ptr[1], ptr[0]});
            } else {
                int32_t filter = samplingModeVal1;
                int32_t mipmap = samplingModeVal2;
                return SkSamplingOptions(static_cast<SkFilterMode>(filter), static_cast<SkMipmapMode>(mipmap));
            }
        }
    }
}

namespace kotlin {
    namespace jvm {
        namespace functions {
            namespace Function0 {
                jclass cls;
                jmethodID invoke;

                void onLoad(JNIEnv* env) {
                    jclass local = env->FindClass("kotlin/jvm/functions/Function0");
                    cls = static_cast<jclass>(env->NewGlobalRef(local));
                    invoke = env->GetMethodID(cls, "invoke", "()Ljava/lang/Object;");
                }

                void onUnload(JNIEnv* env) {
                    env->DeleteGlobalRef(cls);
                }
            }
        }
    }

    void onLoad(JNIEnv* env) {
        jvm::functions::Function0::onLoad(env);
    }
    void onUnload(JNIEnv* env) {
        jvm::functions::Function0::onUnload(env);
    }
}

template<>
jboolean jObjectConvert(JNIEnv* env, jobject obj) {
    return env->CallBooleanMethod(obj, java::lang::Boolean::booleanValue);
}
