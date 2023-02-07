#include <iostream>
#include "SkRuntimeEffect.h"
#include "common.h"

static void deleteRuntimeShaderBuilder(SkRuntimeShaderBuilder* builder) {
    delete builder;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeShaderBuilder__1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deleteRuntimeShaderBuilder));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeShaderBuilder__1nMakeFromRuntimeEffect
  (KNativePointer effectPtr) {
    sk_sp<SkRuntimeEffect> runtimeEffect = sk_ref_sp<SkRuntimeEffect>(reinterpret_cast<SkRuntimeEffect*>(effectPtr));
    SkRuntimeShaderBuilder* builder = new SkRuntimeShaderBuilder(runtimeEffect);
    return reinterpret_cast<KNativePointer>(builder);
}

SKIKO_EXPORT void org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt
  (KNativePointer builderPtr, KInteropPointer uniformName, KInt uniformValue) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(builderPtr);
    runtimeShaderBuilder->uniform(skString(uniformName).c_str()) = uniformValue;
}

SKIKO_EXPORT void org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt2
  (KNativePointer builderPtr, KInteropPointer uniformName, KInt uniformValue1, KInt uniformValue2) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(builderPtr);
    using int2 = std::array<int, 2>;
    runtimeShaderBuilder->uniform(skString(uniformName).c_str()) = int2 {uniformValue1, uniformValue2};
}

SKIKO_EXPORT void org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt3
  (KNativePointer builderPtr, KInteropPointer uniformName, KInt uniformValue1, KInt uniformValue2, KInt uniformValue3) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(builderPtr);
    using int3 = std::array<int, 3>;
    runtimeShaderBuilder->uniform(skString(uniformName).c_str()) = int3 {uniformValue1, uniformValue2, uniformValue3};
}

SKIKO_EXPORT void org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt4
  (KNativePointer builderPtr, KInteropPointer uniformName, KInt uniformValue1, KInt uniformValue2, KInt uniformValue3, KInt uniformValue4) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(builderPtr);
    using int4 = std::array<int, 4>;
    runtimeShaderBuilder->uniform(skString(uniformName).c_str()) = int4 {uniformValue1, uniformValue2, uniformValue3, uniformValue4};
}

SKIKO_EXPORT void org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat
  (KNativePointer builderPtr, KInteropPointer uniformName, KFloat uniformValue) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(builderPtr);
    runtimeShaderBuilder->uniform(skString(uniformName).c_str()) = uniformValue;
}

SKIKO_EXPORT void org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat2
  (KNativePointer builderPtr, KInteropPointer uniformName, KFloat uniformValue1, KFloat uniformValue2) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(builderPtr);
    using float2 = std::array<float, 2>;
    runtimeShaderBuilder->uniform(skString(uniformName).c_str()) = float2 {uniformValue1, uniformValue2};
}

SKIKO_EXPORT void org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat3
  (KNativePointer builderPtr, KInteropPointer uniformName, KFloat uniformValue1, KFloat uniformValue2, KFloat uniformValue3) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(builderPtr);
    using float3 = std::array<float, 3>;
    runtimeShaderBuilder->uniform(skString(uniformName).c_str()) = float3 {uniformValue1, uniformValue2, uniformValue3};
}

SKIKO_EXPORT void org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat4
  (KNativePointer builderPtr, KInteropPointer uniformName, KFloat uniformValue1, KFloat uniformValue2, KFloat uniformValue3, KFloat uniformValue4) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(builderPtr);
    using float4 = std::array<float, 4>;
    runtimeShaderBuilder->uniform(skString(uniformName).c_str()) = float4 {uniformValue1, uniformValue2, uniformValue3, uniformValue4};
}

SKIKO_EXPORT void org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloatMatrix22
  (KNativePointer builderPtr, KInteropPointer uniformName, KFloat* uniformMatrix22) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(builderPtr);
    runtimeShaderBuilder->uniform(skString(uniformName).c_str()) =
        std::array<float, 4> {uniformMatrix22[0], uniformMatrix22[1], uniformMatrix22[2], uniformMatrix22[3]};
}

SKIKO_EXPORT void org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloatMatrix33
  (KNativePointer builderPtr, KInteropPointer uniformName, KFloat* uniformMatrix33) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(builderPtr);
    runtimeShaderBuilder->uniform(skString(uniformName).c_str()) = uniformMatrix33;
}

SKIKO_EXPORT void org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloatMatrix44
  (KNativePointer builderPtr, KInteropPointer uniformName, KFloat* uniformMatrix44) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(builderPtr);
    runtimeShaderBuilder->uniform(skString(uniformName).c_str()) = uniformMatrix44;
}

SKIKO_EXPORT void org_jetbrains_skia_RuntimeShaderBuilder__1nChildShader
  (KNativePointer builderPtr, KInteropPointer childName, KNativePointer childShaderPtr) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(builderPtr);
    sk_sp<SkShader> shader = sk_ref_sp<SkShader>(reinterpret_cast<SkShader*>(childShaderPtr));
    runtimeShaderBuilder->child(skString(childName).c_str()) = shader;
}

SKIKO_EXPORT void org_jetbrains_skia_RuntimeShaderBuilder__1nChildColorFilter
  (KNativePointer builderPtr, KInteropPointer childName, KNativePointer childColorFilterPtr) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(builderPtr);
    sk_sp<SkColorFilter> colorFilter = sk_ref_sp<SkColorFilter>(reinterpret_cast<SkColorFilter*>(childColorFilterPtr));
    runtimeShaderBuilder->child(skString(childName).c_str()) = colorFilter;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeShaderBuilder__1nMakeShader
  (KNativePointer builderPtr, KFloat* localMatrixArr) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(builderPtr);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(localMatrixArr);
    sk_sp<SkShader> shader = runtimeShaderBuilder->makeShader(localMatrix.get());
    return reinterpret_cast<KNativePointer>(shader.release());
}
