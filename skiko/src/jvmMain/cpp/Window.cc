#include <jni.h>

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skiko_SkiaWindow_nativeMethod(JNIEnv *env, jobject thiz,
                                                 jlong param) {
  return param + 1;
}