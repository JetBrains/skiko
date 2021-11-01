#include <jni.h>
#include "../interop.hh"
#include "interop.hh"

namespace skija {
    namespace svg {
        namespace SVGLength {
            jclass cls;
            jmethodID ctor;

            void onLoad(JNIEnv* env) {
                jclass local = env->FindClass("org/jetbrains/skia/svg/SVGLength");
                cls  = static_cast<jclass>(env->NewGlobalRef(local));
                ctor = env->GetMethodID(cls, "<init>", "(FI)V");
            }

            void onUnload(JNIEnv* env) {
                env->DeleteGlobalRef(cls);
            }

            jobject toJava(JNIEnv* env, SkSVGLength length) {
                return env->NewObject(cls, ctor, length.value(), static_cast<jint>(length.unit()));
            }

            void copyToInterop(JNIEnv* env, const SkSVGLength& length, jintArray dst) {
                jint result[2] = { rawBits(length.value()), static_cast<jint>(length.unit()) };
                env->SetIntArrayRegion(dst, 0, 2, result);
            }
        }

        namespace SVGPreserveAspectRatio {
            jclass cls;
            jmethodID ctor;

            void onLoad(JNIEnv* env) {
                jclass local = env->FindClass("org/jetbrains/skia/svg/SVGPreserveAspectRatio");
                cls  = static_cast<jclass>(env->NewGlobalRef(local));
                ctor = env->GetMethodID(cls, "<init>", "(II)V");
            }

            void onUnload(JNIEnv* env) {
                env->DeleteGlobalRef(cls);
            }

            jobject toJava(JNIEnv* env, SkSVGPreserveAspectRatio ratio) {
                return env->NewObject(cls, ctor, static_cast<jint>(ratio.fAlign), static_cast<jint>(ratio.fScale));
            }

            void copyToInterop(JNIEnv* env, const SkSVGPreserveAspectRatio& aspectRatio, jintArray dst) {
                jint data[2] { static_cast<jint>(aspectRatio.fAlign), static_cast<jint>(aspectRatio.fScale) };
                env->SetIntArrayRegion(dst, 0, 2, data);
            }
        }

        void onLoad(JNIEnv* env) {
            SVGLength::onLoad(env);
            SVGPreserveAspectRatio::onLoad(env);
        }

        void onUnload(JNIEnv* env) {
            SVGPreserveAspectRatio::onUnload(env);
            SVGLength::onUnload(env);
        } 
    }
}