#include <iostream>
#include "SkRuntimeEffect.h"
#include "common.h"

static void deleteRuntimeShaderBuilder(SkRuntimeShaderBuilder* builder) {
    delete builder;
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeShaderBuilder_1nGetFinalizer() {
    return reinterpret_cast<KNativePointer>((&deleteRuntimeShaderBuilder));
}

SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeShaderBuilder__1nMakeFromRuntimeEffect
  (KNativePointer effectPtr) {
    sk_sp<SkRuntimeEffect> runtimeEffect = sk_ref_sp<SkRuntimeEffect>(reinterpret_cast<SkRuntimeEffect*>(effectPtr));
    SkRuntimeShaderBuilder* builder = new SkRuntimeShaderBuilder(runtimeEffect);
    return reinterpret_cast<KNativePointer>(builder);
}

SKIKO_EXPORT void org_jetbrains_skia_RuntimeShaderBuilder__1nUniformInt
  (KNativePointer builderPtr, KInteropPointer uniformNameObj, KInt uniformValue) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(builderPtr);
    runtimeShaderBuilder->uniform(skString(uniformNameObj)) = uniformValue;
}

SKIKO_EXPORT void org_jetbrains_skia_RuntimeShaderBuilder__1nUniformFloat
  (KNativePointer builderPtr, KInteropPointer uniformNameObj, KFloat uniformValue) {
    SkRuntimeShaderBuilder* runtimeShaderBuilder = reinterpret_cast<SkRuntimeShaderBuilder*>(builderPtr);
    runtimeShaderBuilder->uniform(skString(uniformNameObj)) = uniformValue;
}
