#include <jni.h>

#include "SkRuntimeEffect.h"
#include "interop.hh"

static void deleteRuntimeShaderBuilder(SkRuntimeShaderBuilder* builder) {
    delete builder;
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderExternalKt_RuntimeShaderBuilder_1nGetFinalizer(JNIEnv* env, jclass jclass) {
    return static_cast<jlong>(reinterpret_cast<uintptr_t>(&deleteRuntimeShaderBuilder));
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderExternalKt_RuntimeShaderBuilder_1nMakeFromRuntimeEffect
  (JNIEnv* env, jclass jclass, jlong effectPtr) {
    sk_sp<SkRuntimeEffect> runtimeEffect = sk_ref_sp<SkRuntimeEffect>(reinterpret_cast<SkRuntimeEffect*>(static_cast<uintptr_t>(effectPtr)));
    SkRuntimeShaderBuilder* builder = new SkRuntimeShaderBuilder(runtimeEffect);
    return reinterpret_cast<jlong>(builder);
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderExternalKt_RuntimeShaderBuilder_1nUniformInt
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring uniformName, jint uniformValue) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = jlongToPtr<SkRuntimeShaderBuilder*>(builderPtr);
    runtimeShaderBuilder->uniform(skString(env, uniformName).c_str()) = uniformValue;
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderExternalKt_RuntimeShaderBuilder_1nUniformInt2
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring uniformName, jint uniformValue1, jint uniformValue2) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = jlongToPtr<SkRuntimeShaderBuilder*>(builderPtr);
    using int2 = std::array<int, 2>;
    runtimeShaderBuilder->uniform(skString(env, uniformName).c_str()) = int2 {uniformValue1, uniformValue2};
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderExternalKt_RuntimeShaderBuilder_1nUniformInt3
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring uniformName, jint uniformValue1, jint uniformValue2, jint uniformValue3) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = jlongToPtr<SkRuntimeShaderBuilder*>(builderPtr);
    using int3 = std::array<int, 3>;
    runtimeShaderBuilder->uniform(skString(env, uniformName).c_str()) = int3 {uniformValue1, uniformValue2, uniformValue3};
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderExternalKt_RuntimeShaderBuilder_1nUniformInt4
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring uniformName, jint uniformValue1, jint uniformValue2, jint uniformValue3, jint uniformValue4) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = jlongToPtr<SkRuntimeShaderBuilder*>(builderPtr);
    using int4 = std::array<int, 4>;
    runtimeShaderBuilder->uniform(skString(env, uniformName).c_str()) = int4 {uniformValue1, uniformValue2, uniformValue3, uniformValue4};
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderExternalKt_RuntimeShaderBuilder_1nUniformFloat
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring uniformName, jfloat uniformValue) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = jlongToPtr<SkRuntimeShaderBuilder*>(builderPtr);
    runtimeShaderBuilder->uniform(skString(env, uniformName).c_str()) = uniformValue;
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderExternalKt_RuntimeShaderBuilder_1nUniformFloat2
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring uniformName, jfloat uniformValue1, jfloat uniformValue2) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = jlongToPtr<SkRuntimeShaderBuilder*>(builderPtr);
    using float2 = std::array<float, 2>;
    runtimeShaderBuilder->uniform(skString(env, uniformName).c_str()) = float2 {uniformValue1, uniformValue2};
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderExternalKt_RuntimeShaderBuilder_1nUniformFloat3
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring uniformName, jfloat uniformValue1, jfloat uniformValue2, jfloat uniformValue3) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = jlongToPtr<SkRuntimeShaderBuilder*>(builderPtr);
    using float3 = std::array<float, 3>;
    runtimeShaderBuilder->uniform(skString(env, uniformName).c_str()) = float3 {uniformValue1, uniformValue2, uniformValue3};
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderExternalKt_RuntimeShaderBuilder_1nUniformFloat4
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring uniformName, jfloat uniformValue1, jfloat uniformValue2, jfloat uniformValue3, jfloat uniformValue4) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = jlongToPtr<SkRuntimeShaderBuilder*>(builderPtr);
    using float4 = std::array<float, 4>;
    runtimeShaderBuilder->uniform(skString(env, uniformName).c_str()) = float4 {uniformValue1, uniformValue2, uniformValue3, uniformValue4};
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderExternalKt_RuntimeShaderBuilder_1nUniformFloatArray
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring uniformName, jfloatArray uniformFloatArray, jint length) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = jlongToPtr<SkRuntimeShaderBuilder*>(builderPtr);
    jfloat* floatArray = static_cast<jfloat*>(env->GetPrimitiveArrayCritical(uniformFloatArray, 0));
    runtimeShaderBuilder->uniform(skString(env, uniformName).c_str()).set(floatArray, length);
    env->ReleasePrimitiveArrayCritical(uniformFloatArray, floatArray, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderExternalKt_RuntimeShaderBuilder_1nUniformFloatMatrix22
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring uniformName, jfloatArray uniformMatrix22) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = jlongToPtr<SkRuntimeShaderBuilder*>(builderPtr);
    jfloat* matrix22 = static_cast<jfloat*>(env->GetPrimitiveArrayCritical(uniformMatrix22, 0));
    runtimeShaderBuilder->uniform(skString(env, uniformName).c_str()) =
        std::array<float, 4> {matrix22[0], matrix22[1], matrix22[2], matrix22[3]};
    env->ReleasePrimitiveArrayCritical(uniformMatrix22, matrix22, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderExternalKt_RuntimeShaderBuilder_1nUniformFloatMatrix33
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring uniformName, jfloatArray uniformMatrix33) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = jlongToPtr<SkRuntimeShaderBuilder*>(builderPtr);
    std::unique_ptr<SkMatrix> matrix33 = skMatrix(env, uniformMatrix33);
    runtimeShaderBuilder->uniform(skString(env, uniformName).c_str()) = *matrix33;
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderExternalKt_RuntimeShaderBuilder_1nUniformFloatMatrix44
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring uniformName, jfloatArray uniformMatrix44) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = jlongToPtr<SkRuntimeShaderBuilder*>(builderPtr);
    std::unique_ptr<SkM44> matrix44 = skM44(env, uniformMatrix44);
    runtimeShaderBuilder->uniform(skString(env, uniformName).c_str()) = *matrix44;
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderExternalKt_RuntimeShaderBuilder_1nChildShader
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring childName, jlong childShaderPtr) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = jlongToPtr<SkRuntimeShaderBuilder*>(builderPtr);
    sk_sp<SkShader> shader = sk_ref_sp<SkShader>(reinterpret_cast<SkShader*>(static_cast<uintptr_t>(childShaderPtr)));
    runtimeShaderBuilder->child(skString(env, childName).c_str()) = shader;
}

extern "C" JNIEXPORT void JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderExternalKt_RuntimeShaderBuilder_1nChildColorFilter
  (JNIEnv* env, jclass jclass, jlong builderPtr, jstring childName, jlong childColorFilterPtr) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = jlongToPtr<SkRuntimeShaderBuilder*>(builderPtr);
    sk_sp<SkColorFilter> colorFilter = sk_ref_sp<SkColorFilter>(reinterpret_cast<SkColorFilter*>(static_cast<uintptr_t>(childColorFilterPtr)));
    runtimeShaderBuilder->child(skString(env, childName).c_str()) = colorFilter;
}

extern "C" JNIEXPORT jlong JNICALL
Java_org_jetbrains_skia_RuntimeShaderBuilderExternalKt_RuntimeShaderBuilder_1nMakeShader
  (JNIEnv* env, jclass jclass, jlong builderPtr, jfloatArray localMatrixArr) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = jlongToPtr<SkRuntimeShaderBuilder*>(builderPtr);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, localMatrixArr);
    sk_sp<SkShader> shader = runtimeShaderBuilder->makeShader(localMatrix.get());
    return reinterpret_cast<jlong>(shader.release());
}
