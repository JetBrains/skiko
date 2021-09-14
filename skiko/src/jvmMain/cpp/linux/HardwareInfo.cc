#include <sstream>
#include <iostream>
#include "jni_helpers.h"

extern "C"
{
    JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_RenderExceptionsHandlerKt_getNativeGraphicsAdapterInfo(
        JNIEnv *env, jobject object)
    {
        return env->NewStringUTF("Can't get VC info.");
    }

    JNIEXPORT jstring JNICALL Java_org_jetbrains_skiko_RenderExceptionsHandlerKt_getNativeCpuInfo(
        JNIEnv *env, jobject object)
    {
        return env->NewStringUTF("Can't get CPU info.");
    }
}