#include <array>
#include <cstring>
#include "interop.hh"
#include <iostream>
#include <jni.h>
#include <memory>

namespace skiko {
    namespace DisplayLink {
        jclass cls;
        jmethodID outputCallback;

        void onLoad(JNIEnv* env) {
            jclass local = env->FindClass("org/jetbrains/skiko/redrawer/macos/DisplayLink");
            cls  = static_cast<jclass>(env->NewGlobalRef(local));
            outputCallback = env->GetMethodID(cls, "outputCallback", "()V");
        }

        void onUnload(JNIEnv* env) {
            env->DeleteGlobalRef(cls);
        }
    }

    void onLoad(JNIEnv* env) {
        DisplayLink::onLoad(env);
    }

    void onUnload(JNIEnv* env) {
        DisplayLink::onUnload(env);
    }
}
