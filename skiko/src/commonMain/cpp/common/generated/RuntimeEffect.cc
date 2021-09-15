
// This file has been auto generated.

#include "SkRuntimeEffect.h"
#include "common.h"

SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1nMakeShader
    (KNativePointer ptr, KNativePointer uniformPtr, KNativePointerArray childrenPtrsArr, KFloat* localMatrixArr, KBoolean isOpaque) {
    TODO("implement org_jetbrains_skia_RuntimeEffect__1nMakeShader");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1nMakeShader
    (KNativePointer ptr, KNativePointer uniformPtr, KNativePointerArray childrenPtrsArr, KFloat* localMatrixArr, KBoolean isOpaque) {
    SkRuntimeEffect* runtimeEffect = KNativePointerToPtr<SkRuntimeEffect*>(ptr);
    SkData* uniform = KNativePointerToPtr<SkData*>(uniformPtr);
    std::unique_ptr<SkMatrix> localMatrix = skMatrix(env, localMatrixArr);

    jsize childCount = env->GetArrayLength(childrenPtrsArr);
    KNativePointer* childrenPtrs = env->GetLongArrayElements(childrenPtrsArr, 0);
    std::vector<sk_sp<SkShader>> children(childCount);
    for (size_t i = 0; i < childCount; i++) {
        SkShader* si = KNativePointerToPtr<SkShader*>(childrenPtrs[i]);
        children[i] = sk_ref_sp(si);
    }
    env->ReleaseLongArrayElements(childrenPtrsArr, childrenPtrs, 0);

    sk_sp<SkShader> shader = runtimeEffect->makeShader(sk_ref_sp<SkData>(uniform),
                                                       children.data(),
                                                       childCount,
                                                       localMatrix.get(),
                                                       isOpaque);
    return ptrToKNativePointer(shader.release());
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1nMakeForShader
    (KInteropPointer sksl) {
    TODO("implement org_jetbrains_skia_RuntimeEffect__1nMakeForShader");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1nMakeForShader
    (KInteropPointer sksl) {
    SkString skslProper = skString(env, sksl);
    SkRuntimeEffect::Result result = SkRuntimeEffect::MakeForShader(skslProper);
    if (result.errorText.isEmpty()) {
        sk_sp<SkRuntimeEffect> effect = result.effect;
        return ptrToKNativePointer(effect.release());
    } else {
        env->ThrowNew(java::lang::RuntimeException::cls, result.errorText.c_str());
        return 0;
    }
}
#endif



SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1nMakeForColorFilter
    (KInteropPointer sksl) {
    TODO("implement org_jetbrains_skia_RuntimeEffect__1nMakeForColorFilter");
}
     
#if 0 
SKIKO_EXPORT KNativePointer org_jetbrains_skia_RuntimeEffect__1nMakeForColorFilter
    (KInteropPointer sksl) {
    SkString skslProper = skString(env, sksl);
    SkRuntimeEffect::Result result = SkRuntimeEffect::MakeForColorFilter(skslProper);
    if (result.errorText.isEmpty()) {
        return ptrToKNativePointer(result.effect.release());
    } else {
        env->ThrowNew(java::lang::RuntimeException::cls, result.errorText.c_str());
        return 0;
    }
}
#endif

