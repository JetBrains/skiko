#pragma once
#include <jni.h>
#include "SkSVGTypes.h"

namespace skija {
    namespace svg {
        namespace SVGLength {
            extern jclass cls;
            extern jmethodID ctor;
            void onLoad(JNIEnv* env);
            void onUnload(JNIEnv* env);
            jobject toJava(JNIEnv* env, SkSVGLength length);
            void copyToInterop(JNIEnv* env, const SkSVGLength& length, jintArray dst);
        }

        namespace SVGPreserveAspectRatio {
            extern jclass cls;
            extern jmethodID ctor;
            void onLoad(JNIEnv* env);
            void onUnload(JNIEnv* env);
            jobject toJava(JNIEnv* env, SkSVGPreserveAspectRatio ratio);
            void copyToInterop(JNIEnv* env, const SkSVGPreserveAspectRatio& aspectRatio, jintArray dst);
        }

        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
    }
}