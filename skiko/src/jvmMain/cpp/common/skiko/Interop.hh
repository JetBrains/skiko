#pragma once
#include <iostream>
#include <jni.h>
#include <memory>
#include <vector>
#include "mppinterop.h"

namespace skiko {
    namespace DisplayLink {
        extern jclass cls;
        extern jmethodID outputCallback;
        void onLoad(JNIEnv* env);
        void onUnload(JNIEnv* env);
    }

    void onLoad(JNIEnv* env);
    void onUnload(JNIEnv* env);
}
