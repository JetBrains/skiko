#include <jni.h>
#include "../interop.hh"
#include "../shaper/interop.hh"
#include "../skottie/interop.hh"
#include "../paragraph/interop.hh"
#include "../svg/interop.hh"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), SKIKO_JNI_VERSION) != JNI_OK)
        return JNI_ERR;

    return SKIKO_JNI_VERSION;
}

extern "C" JNIEXPORT void JNICALL Java_org_jetbrains_skia_impl_Library__1nAfterLoad
  (JNIEnv* env, jclass jclass) {
    env->EnsureLocalCapacity(64);
    java::onLoad(env);
    skija::onLoad(env);
    skija::shaper::onLoad(env);
    skija::skottie::onLoad(env);
    skija::paragraph::onLoad(env);
    skija::svg::onLoad(env);
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), SKIKO_JNI_VERSION) != JNI_OK)
        return;

    skija::svg::onUnload(env);
    skija::paragraph::onUnload(env);
    skija::skottie::onUnload(env);
    skija::shaper::onUnload(env);
    skija::onUnload(env);
    java::onUnload(env);
}
