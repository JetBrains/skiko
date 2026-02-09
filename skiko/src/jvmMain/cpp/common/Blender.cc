#include <iostream>
#include <jni.h>
#include "interop.hh"
#include "SkBlender.h"
#include "SkBlenders.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_BlenderKt__1nMakeArithmetic
  (JNIEnv* env, jclass jclass, jfloat k1, jfloat k2, jfloat k3, jfloat k4, jboolean enforcePMColor) {
    SkBlender* ptr = SkBlenders::Arithmetic(k1, k2, k3, k4, enforcePMColor).release();
    return reinterpret_cast<jlong>(ptr);
}

