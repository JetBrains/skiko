#include <jni.h>

#include "SkRuntimeEffect.h"
#include "interop.hh"

static void deleteRuntimeShaderBuilder(SkRuntimeShaderBuilder* builder) {
    delete builder;
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderKt_RuntimeShaderBuilder_1nGetFinalizer(JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteRuntimeShaderBuilder));
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderKt__1nMakeFromRuntimeEffect
  (JNIEnv* env, jclass jclass, jlong effectPtr) {
    sk_sp<SkRuntimeEffect> runtimeEffect = sk_ref_sp<SkRuntimeEffect>(reinterpret_cast<SkRuntimeEffect*>(static_cast<uintptr_t>(effectPtr)));
    SkRuntimeShaderBuilder* builder = new SkRuntimeShaderBuilder(runtimeEffect);
    return reinterpret_cast<jlong>(builder);
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderKt__1nUniformInt
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring uniformName, jint uniformValue) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = jlongToPtr<SkRuntimeShaderBuilder*>(builderPtr);
    runtimeShaderBuilder->uniform(skString(env, uniformName).c_str()) = uniformValue;
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderKt__1nUniformFloat
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring uniformName, jfloat uniformValue) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = jlongToPtr<SkRuntimeShaderBuilder*>(builderPtr);
    runtimeShaderBuilder->uniform(skString(env, uniformName).c_str()) = uniformValue;
}
