#include <jni.h>
#include "../skottie/skottie_interop.hh"

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_skottie_SkottieLibraryKt__1nAfterLoad
  (JNIEnv* env, jclass jclass) {
    env->EnsureLocalCapacity(64);
    skija::skottie::onLoad(env);
}
